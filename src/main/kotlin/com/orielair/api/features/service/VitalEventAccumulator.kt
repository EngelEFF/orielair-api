package com.orielair.api.features.service


import com.orielair.api.shared.event.VitalRecorded
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import java.util.ArrayDeque
import java.util.UUID


/**
 * Associates a vital event with its original Kafka offset.
 */
// Rename to ReceivedBatchRecord
data class ReceivedRecord(
    val vital: VitalRecorded,
    val offset: Long
)


/**
 * Reports the accumulation result for a user's batch
 * and the current state of that user's FIFO queue.
 */
data class UserBatchAccumulationStatus(
    val userId: String,
    val batchDeliveryId: UUID,
    val receivedRecordCount: Int,
    val pendingBatchCount: Int,
    val pendingRecordCount: Int
)

/**
 * Represents the vital records belonging to one user
 * within an incoming consumer delivery.
 *
 * Batches created from the same incoming message share
 * the same batchDeliveryId.
 */
data class ReceivedBatch(
    val batchDeliveryId: UUID,
    val records: List<VitalRecorded>
)






















/**
 * Maintains independent FIFO queues of received batches
 * for each user and prediction horizon.
 *
 * Incoming consumer deliveries are separated by userId.
 * Each user receives a separate ReceivedBatch containing
 * their records from that delivery.
 *
 * The two prediction horizons maintain independent collections.
 */
@Service
class VitalEventAccumulator {

    private companion object {
        val logger = LoggerFactory.getLogger(VitalEventAccumulator::class.java)
    }
    // Independent FIFO containers for each prediction horizon.
    // Each container maintains a separate queue for every user.
    private val shortHorizonBatches =
        mutableMapOf<String, ArrayDeque<ReceivedBatch>>()

    private val longHorizonBatches =
        mutableMapOf<String, ArrayDeque<ReceivedBatch>>()

    // Each horizon has its own lock.
    // This protects concurrent access to its respective collection
    // while allowing both horizons to operate independently.
    private val shortHorizonLock = Any()
    private val longHorizonLock = Any()

    /**
     * Organizes a short-horizon delivery into user-specific batches.
     */
    fun accumulateShortHorizon(
        message: Message<List<VitalRecorded>>
    ): List<UserBatchAccumulationStatus> =
        accumulate(
            message,
            shortHorizonBatches,
            shortHorizonLock,
            "SHORT-HORIZON"
        )

    /**
     * Organizes a long-horizon delivery into user-specific batches.
     */
    fun accumulateLongHorizon(
        message: Message<List<VitalRecorded>>
    ): List<UserBatchAccumulationStatus> =
        accumulate(
            message,
            longHorizonBatches,
            longHorizonLock,
            "LONG-HORIZON"
        )

    /**
     * Retrieves the next short-horizon batch for a user
     * without removing it from the FIFO queue.
     */
    fun peekUserShortHorizonBatch(
        userId: String
    ): ReceivedBatch? =
        synchronized(shortHorizonLock) {
            shortHorizonBatches[userId]?.peekFirst()
        }

    /**
     * Retrieves the next long-horizon batch for a user
     * without removing it from the FIFO queue.
     */
    fun peekUserLongHorizonBatch(
        userId: String
    ): ReceivedBatch? =
        synchronized(longHorizonLock) {
            longHorizonBatches[userId]?.peekFirst()
        }

    /**
     * Retrieves all short-horizon batches originating
     * from a particular consumer delivery.
     *
     * The result associates each batch with its userId.
     */
    fun getUserShortHorizonBatches(
        batchDeliveryId: UUID
    ): Map<String, ReceivedBatch> =
        getBatchesByDeliveryId(
            batchDeliveryId,
            shortHorizonBatches,
            shortHorizonLock
        )

    /**
     * Retrieves all long-horizon batches originating
     * from a particular consumer delivery.
     *
     * The result associates each batch with its userId.
     */
    fun getUserLongHorizonBatches(
        batchDeliveryId: UUID
    ): Map<String, ReceivedBatch> =
        getBatchesByDeliveryId(
            batchDeliveryId,
            longHorizonBatches,
            longHorizonLock
        )

    /**
     * Removes the head short-horizon batch after its
     * processing has completed successfully.
     *
     * The expected batchDeliveryId prevents removal
     * of a different batch if the queue has changed.
     */
    fun removeProcessedShortHorizonBatch(
        userId: String,
        batchDeliveryId: UUID
    ): ReceivedBatch =
        removeProcessedBatch(
            userId,
            batchDeliveryId,
            shortHorizonBatches,
            shortHorizonLock
        )

    /**
     * Removes the head long-horizon batch after its
     * processing has completed successfully.
     *
     * The expected batchDeliveryId prevents removal
     * of a different batch if the queue has changed.
     */
    fun removeProcessedLongHorizonBatch(
        userId: String,
        batchDeliveryId: UUID
    ): ReceivedBatch =
        removeProcessedBatch(
            userId,
            batchDeliveryId,
            longHorizonBatches,
            longHorizonLock
        )


    /**
     * Groups the records in an incoming delivery by userId
     * and appends one ReceivedBatch per user to the appropriate
     * horizon-specific FIFO queues.
     *
     * All user-specific batches created from the same incoming
     * message share one batchDeliveryId.
     *
     * Each invocation uses its horizon-specific collection
     * and lock. This protects concurrent mutations within
     * that horizon without blocking the other horizon.
     *
     * Logs the accumulation result and pending queue statistics
     * for the corresponding prediction horizon.
     */
    private fun accumulate(
        message: Message<List<VitalRecorded>>,
        container: MutableMap<String, ArrayDeque<ReceivedBatch>>,
        lock: Any,
        horizon: String
    ): List<UserBatchAccumulationStatus> {

        val userBatchRecords = extractUserBatchRecords(message)

        if (userBatchRecords.isEmpty()) {
            logger.debug(
                "📭 [{}] Received an empty Kafka delivery. Nothing to accumulate.",
                horizon
            )
            return emptyList()
        }

        val batchDeliveryId = UUID.randomUUID()

        val statuses = synchronized(lock) {

            userBatchRecords.map { (userId, records) ->

                val userBatches = container.getOrPut(userId) {
                    ArrayDeque()
                }

                val receivedBatch = ReceivedBatch(
                    batchDeliveryId = batchDeliveryId,
                    records = records
                )

                userBatches.addLast(receivedBatch)

                UserBatchAccumulationStatus(
                    userId = userId,
                    batchDeliveryId = batchDeliveryId,
                    receivedRecordCount = records.size,
                    pendingBatchCount = userBatches.size,
                    pendingRecordCount = userBatches.sumOf {
                        it.records.size
                    }
                )
            }
        }

        // Aggregate the pending state for users affected by this delivery.
        val receivedRecordCount = statuses.sumOf {
            it.receivedRecordCount
        }

        val pendingBatchCount = statuses.sumOf {
            it.pendingBatchCount
        }

        val pendingRecordCount = statuses.sumOf {
            it.pendingRecordCount
        }

        logger.info(
            "📥 [{}] Delivery accumulated successfully | " +
                    "deliveryId={} | records={} | users={} | " +
                    "pendingBatches={} | pendingRecords={}",
            horizon,
            batchDeliveryId,
            receivedRecordCount,
            statuses.size,
            pendingBatchCount,
            pendingRecordCount
        )

        // Detailed statistics for individual users.
        statuses.forEach { status ->

            logger.debug(
                "👤 [{}] User accumulated | userId={} | " +
                        "receivedRecords={} | pendingBatches={} | pendingRecords={}",
                horizon,
                status.userId,
                status.receivedRecordCount,
                status.pendingBatchCount,
                status.pendingRecordCount
            )
        }

        return statuses
    }
    /**
     * Groups the incoming vital records by userId.
     *
     * Records belonging to the same user remain in
     * their original order within the incoming delivery.
     */
    private fun extractUserBatchRecords(
        message: Message<List<VitalRecorded>>
    ): Map<String, List<VitalRecorded>> {

        val vitals = message.payload

        if (vitals.isEmpty()) {
            return emptyMap()
        }

        val userBatchRecords =
            linkedMapOf<String, MutableList<VitalRecorded>>()

        vitals.forEach { vital ->

            require(vital.userId.toString().isNotBlank()) {
                "VitalRecorded contains an empty userId."
            }

            userBatchRecords
                .getOrPut(vital.userId.toString()) { mutableListOf() }
                .add(vital)
        }

        return userBatchRecords.mapValues { (_, records) ->
            records.toList()
        }
    }

    /**
     * Retrieves the user-specific batches associated with
     * a particular consumer delivery.
     *
     * The search is performed across the horizon's user queues
     * while holding the corresponding collection lock.
     */
    private fun getBatchesByDeliveryId(
        batchDeliveryId: UUID,
        container: MutableMap<String, ArrayDeque<ReceivedBatch>>,
        lock: Any
    ): Map<String, ReceivedBatch> =
        synchronized(lock) {

            container.mapNotNull { (userId, userBatches) ->

                userBatches
                    .firstOrNull {
                        it.batchDeliveryId == batchDeliveryId
                    }
                    ?.let { userId to it }
            }.toMap()
        }

    /**
     * Removes only the expected FIFO head batch for a user.
     *
     * The caller must ensure that processing has completed
     * successfully before invoking this operation.
     */
    private fun removeProcessedBatch(
        userId: String,
        batchDeliveryId: UUID,
        container: MutableMap<String, ArrayDeque<ReceivedBatch>>,
        lock: Any
    ): ReceivedBatch =
        synchronized(lock) {

            val userBatches = container[userId]
                ?: throw IllegalStateException(
                    "No pending batches exist for user $userId."
                )

            val firstBatch = userBatches.peekFirst()
                ?: throw IllegalStateException(
                    "The batch queue is empty for user $userId."
                )

            check(firstBatch.batchDeliveryId == batchDeliveryId) {
                "FIFO violation for user $userId. " +
                        "Expected batch ${firstBatch.batchDeliveryId}, " +
                        "but received removal request for $batchDeliveryId."
            }

            val removedBatch = userBatches.removeFirst()

            if (userBatches.isEmpty()) {
                container.remove(userId)
            }
            removedBatch
        }
}