# CdcReaderProperties

type
readerName
outboxId
leadershipLockPath
monitoringRetryIntervalInMilliseconds
monitoringRetryAttempts

# Leadership Controller CdcReader LifeCycle for scalable

start          --> start LeaderSelector
leaderSelected --> CdcReader start
leaderRemoved  --> CdcReader stop
stop           --> stop CdcReader and LeaderSelector
fail callback  --> relinquish

# Offset

# CdcReader HealthCheck

db connection
maxAttemptsForTransactionLogConnection
offset save
publish msg
Thread.sleep

# CdcReader Metrics

readerName
isLeader
messages.processed
connection.attempts

# CdcReaderProcessingStatusService



