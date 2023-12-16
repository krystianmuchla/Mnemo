package com.github.krystianmuchla.mnemo.instant

import java.time.Instant
import java.time.temporal.ChronoUnit

class InstantFactory {
    companion object {
        fun create(): Instant {
            return Instant.now().truncatedTo(ChronoUnit.MILLIS)
        }
    }
}
