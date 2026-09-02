package com.quantorbital.core.memory;

import com.quantorbital.core.model.TelemetryPacket;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Low-level, off-heap telemetry buffer designed for zero-GC footprint in aerospace software.
 * Uses Java's modern Foreign Function & Memory API (Project Panama) to manage raw native memory.
 */
public class NativeSpaceMemoryBuffer implements AutoCloseable {

    private final Arena arena;
    private final MemorySegment segment;
    private final long capacity;
    private final long elementSize;
    private final ReentrantLock lock = new ReentrantLock();
    private long writeOffset = 0;

    public NativeSpaceMemoryBuffer(long capacity) {
        this.capacity = capacity;
        this.arena = Arena.ofShared(); // Thread-safe memory region
        
        // Define structure size: 
        // 8 bytes (timestamp) + 8 bytes (batteryTemp) + 8 bytes (fuel) + 8 bytes (radiation) + 8 bytes (velocity) = 40 bytes
        this.elementSize = 40; 
        this.segment = arena.allocate(capacity * elementSize);
    }

    /**
     * Pushes a telemetry packet directly into off-heap native memory.
     */
    public void push(TelemetryPacket packet) {
        lock.lock();
        try {
            if (writeOffset >= capacity) {
                writeOffset = 0; // Circular buffer overwrite mechanism
            }

            long currentPos = writeOffset * elementSize;

            // Direct byte-level writing to native memory addresses
            segment.set(ValueLayout.JAVA_LONG, currentPos, packet.timestamp().toEpochMilli());
            segment.set(ValueLayout.JAVA_DOUBLE, currentPos + 8, packet.batteryTemperature());
            segment.set(ValueLayout.JAVA_DOUBLE, currentPos + 16, packet.fuelLevel());
            segment.set(ValueLayout.JAVA_DOUBLE, currentPos + 24, packet.radiationLevel());
            segment.set(ValueLayout.JAVA_DOUBLE, currentPos + 32, packet.velocity());

            writeOffset++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reads a telemetry frame from a specific index without engaging JVM Heap objects.
     */
    public Optional<TelemetryPacket> read(long index, String satelliteId) {
        if (index >= capacity || index < 0) {
            return Optional.empty();
        }

        long currentPos = index * elementSize;
        long epochMilli = segment.get(ValueLayout.JAVA_LONG, currentPos);
        
        if (epochMilli == 0) return Optional.empty(); // Unwritten slot

        double battery = segment.get(ValueLayout.JAVA_DOUBLE, currentPos + 8);
        double fuel = segment.get(ValueLayout.JAVA_DOUBLE, currentPos + 16);
        double radiation = segment.get(ValueLayout.JAVA_DOUBLE, currentPos + 24);
        double velocity = segment.get(ValueLayout.JAVA_DOUBLE, currentPos + 32);

        return Optional.of(new TelemetryPacket(
            satelliteId,
            Instant.ofEpochMilli(epochMilli),
            battery,
            fuel,
            radiation,
            velocity
        ));
    }

    @Override
    public void close() {
        // Deterministic memory deallocation. Immediate RAM release, no waiting for GC!
        arena.close();
    }
}
