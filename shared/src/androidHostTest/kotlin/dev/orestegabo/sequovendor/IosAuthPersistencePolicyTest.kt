package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.auth.*
import dev.orestegabo.sequovendor.core.auth.KeychainWriteResult.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class IosAuthPersistencePolicyTest {
    @Test fun replacementUpdatesWithoutDeletingOrAddingExistingCredential() {
        var adds = 0
        writeKeychainValue(update = { Success }, add = { adds++; Failed })
        assertEquals(0, adds)
    }

    @Test fun updateFailureDoesNotReplaceExistingCredential() {
        var stored = "old"
        assertFailsWith<IllegalStateException> {
            writeKeychainValue(update = { Failed }, add = { stored = "new"; Success })
        }
        assertEquals("old", stored)
    }

    @Test fun racedCreationRetriesUpdateAndAnyWriteFailureIsReported() {
        var updates = 0
        writeKeychainValue(update = { if (++updates == 1) NotFound else Success }, add = { Duplicate })
        assertEquals(2, updates)
        assertFailsWith<IllegalStateException> { writeKeychainValue(update = { NotFound }, add = { Failed }) }
    }

    @Test fun validLegacySessionMovesOnlyAfterSecureReadback() = runBlocking {
        var legacy: String? = "valid"
        var secure: String? = null
        val result = readMigratingAuthValue({ secure }, { legacy }, { secure = it }, {
            assertEquals("valid", secure)
            legacy = null
        }, { it == "valid" })
        assertEquals("valid", result)
        assertNull(legacy)
        assertEquals("valid", secure)
    }

    @Test fun failedWritePreservesLegacySession() = runBlocking {
        var legacy: String? = "valid"
        assertFailsWith<IllegalStateException> {
            readMigratingAuthValue({ null }, { legacy }, { error("Unavailable") }, { legacy = null }, { true })
        }
        assertEquals("valid", legacy)
    }

    @Test fun failedReadbackPreservesLegacySession() = runBlocking {
        var legacy: String? = "valid"
        assertFailsWith<IllegalStateException> {
            readMigratingAuthValue({ null }, { legacy }, {}, { legacy = null }, { true })
        }
        assertEquals("valid", legacy)
    }

    @Test fun unavailableKeychainNeverFallsBackToPlaintext() = runBlocking {
        var removed = false
        assertFailsWith<IllegalStateException> {
            readMigratingAuthValue({ error("Locked") }, { "valid" }, {}, { removed = true }, { true })
        }
        assertFalse(removed)
    }

    @Test fun newerSecureValueWinsAndLegacyRemovedOnlyAfterCheckedWrite() = runBlocking {
        var legacy: String? = "old"
        var secure: String? = "new"
        var writes = 0
        assertEquals("new", readMigratingAuthValue({ secure }, { legacy }, {
            writes++; secure = it
        }, { legacy = null }, { true }))
        assertEquals(1, writes)
        assertNull(legacy)
        assertEquals("new", secure)
    }

    @Test fun invalidLegacyValueIsNotMigratedOrDiscarded() = runBlocking {
        var legacy: String? = "unsupported"
        var writes = 0
        assertNull(readMigratingAuthValue({ null }, { legacy }, { writes++ }, { legacy = null }, { false }))
        assertEquals("unsupported", legacy)
        assertEquals(0, writes)
    }

    @Test fun newSessionSaveAlsoRetainsLegacyWhenReadbackDoesNotMatch() = runBlocking {
        var legacy: String? = "old"
        assertFailsWith<IllegalStateException> {
            saveMigratedAuthValue("new", {}, { "old" }, { legacy = null })
        }
        assertEquals("old", legacy)
    }
}
