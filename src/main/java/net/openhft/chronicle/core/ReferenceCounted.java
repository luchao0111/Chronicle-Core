/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * A resource which is reference counted and freed when the refCount drop to 0.
 *
 * @deprecated Use net.openhft.chronicle.core.io.ReferenceCounted
 */
public interface ReferenceCounted {

    /**
     * Reserves a resource or throws an Exception.
     * <p>
     * Each invocation of this method increases the reference count by one.
     *
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */
    void reserve() throws IllegalStateException;

    /**
     * Tries to reserve a resource and returns if the resource could
     * be successfully reserved.
     * <p>
     * Each invocation of this method increases the reference count by one.
     *
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */

    boolean tryReserve() throws IllegalStateException;

    /**
     * Releases a resource.
     * <p>
     * Each invocation of this method decreases the reference count by one.
     *
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */
    void release() throws IllegalStateException;

    /**
     * Returns the reference count for this resource.
     *
     * @return the reference count for this resource
     */
    long refCount();


    static void releaseAll(@NotNull List<WeakReference<? extends ReferenceCounted>> refCounts) {
        for (@Nullable WeakReference<? extends ReferenceCounted> refCountRef : refCounts) {
            if (refCountRef == null)
                continue;
            @Nullable ReferenceCounted refCounted = refCountRef.get();
            if (refCounted != null) {
                try {
                    refCounted.release();
                } catch (IllegalStateException e) {
                    LoggerFactory.getLogger(Closeable.class).debug("", e);
                }
            }
        }
    }

    /**
     * Releases a reference counted object if it is ReferenceCounted.
     *
     * @param o to release if ReferenceCounted
     */
    static void release(final Object o) {
        if (o instanceof ReferenceCounted) {
            @NotNull ReferenceCounted rc = (ReferenceCounted) o;
            try {
                rc.release();
            } catch (IllegalStateException e) {
                LoggerFactory.getLogger(Closeable.class).debug("", e);
            }
        }
    }

}