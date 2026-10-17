-- IncidentVault V2 seed data (original, illustrative incidents and runbooks).

-- ---------------------------------------------------------------------------
-- Incident 1: P1 checkout latency spike (RESOLVED)
-- ---------------------------------------------------------------------------
INSERT INTO incidents (title, severity, status, service, started_at, resolved_at, summary)
VALUES (
    'Checkout latency spike - Redis failover misconfig',
    'P1', 'RESOLVED', 'checkout-api',
    TIMESTAMPTZ '2026-09-22 14:05:00+00',
    TIMESTAMPTZ '2026-09-22 17:42:00+00',
    'p99 checkout latency breached the 2000ms SLO during a scheduled Redis failover. '
    || 'The Sentinel failover policy still referenced a decommissioned primary endpoint, '
    || 'so clients kept routing to a dead node until the config was corrected and a '
    || 'controlled failover was forced to the healthy replica.'
);

INSERT INTO timeline_events (incident_id, timestamp, actor, type, message)
SELECT id, TIMESTAMPTZ '2026-09-22 14:07:00+00', 'monitoring', 'ALERT',
       'p99 checkout latency exceeded 2000ms for 5 consecutive minutes (SLO: 800ms)'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-09-22 14:09:00+00', 'priya.nair', 'ACTION',
       'Acknowledged page; opened incident bridge and froze non-urgent deploys to checkout-api'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-09-22 14:21:00+00', 'marcus.chen', 'ACTION',
       'Redis primary showing 42s replica lag and climbing; client timeouts spiking in us-east cache tier'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-09-22 14:35:00+00', 'priya.nair', 'NOTE',
       'Shed 30% of cache-tier traffic to us-west to relieve pressure while diagnosing'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-09-22 15:12:00+00', 'marcus.chen', 'ACTION',
       'Found failover policy still pointing at decommissioned primary endpoint cache-primary-03 (retired Aug 30)'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-09-22 16:05:00+00', 'priya.nair', 'ACTION',
       'Pushed corrected Sentinel config; forced controlled failover to healthy replica cache-primary-05'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-09-22 17:42:00+00', 'priya.nair', 'NOTE',
       'p99 latency under 350ms for 30 minutes; error rate back to baseline. Resolving.'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig';

INSERT INTO log_snippets (incident_id, source, snippet, captured_at)
SELECT id, 'redis cache-primary-05', $$
# role:slave
master_host:cache-primary-03
master_port:6379
master_link_status:down
master_last_io_seconds_ago:42
master_sync_in_progress:0
connected_slaves:0
$$, TIMESTAMPTZ '2026-09-22 14:21:00+00'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig'
UNION ALL
SELECT id, 'checkout-api app log', $$
ERROR o.s.d.r.RedisConnectionFailureException: Unable to connect to cache-primary-03:6379; nested exception is io.lettuce.core.RedisConnectionException: Connection timed out after 2000ms
  at checkout.cache.SessionCache.get(SessionCache.java:112)
  ... 1843 occurrences in 5 minutes ...
$$, TIMESTAMPTZ '2026-09-22 14:18:00+00'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig';

INSERT INTO root_causes (incident_id, hypothesis, validated, evidence)
SELECT id,
       'Sentinel failover policy referenced the decommissioned cache-primary-03 endpoint, so during the scheduled failover window clients kept routing to a dead node until manual intervention.',
       TRUE,
       'Config diff of cache-tier.yaml showed the stale endpoint; replica lag fell from 42s to under 200ms within 2 minutes of the corrected failover; p99 recovered from 2100ms to 320ms.'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig';

INSERT INTO fixes (incident_id, description, pr_url, commit_sha)
SELECT id,
       'Corrected the Sentinel failover endpoint in cache-tier config and added a CI check that fails the build when a referenced cache endpoint is missing from the inventory.',
       'https://github.com/example-org/checkout-api/pull/4821',
       '9f3c2a1bd4e77f0ac51d9c3be882a4f5e6d10c2a'
FROM incidents WHERE title = 'Checkout latency spike - Redis failover misconfig';

-- ---------------------------------------------------------------------------
-- Incident 2: P2 Kafka consumer lag after broker restart (MITIGATED)
-- ---------------------------------------------------------------------------
INSERT INTO incidents (title, severity, status, service, started_at, resolved_at, summary)
VALUES (
    'Kafka consumer lag after broker restart',
    'P2', 'MITIGATED', 'order-events',
    TIMESTAMPTZ '2026-10-08 09:12:00+00',
    NULL,
    'Consumer group order-events-proc fell 500k+ messages behind on topic order-events after '
    || 'broker kafka-02 restarted during a rolling OS patch. Lag is draining after a temporary '
    || 'scale-up; incident stays mitigated until the backlog fully clears.'
);

INSERT INTO timeline_events (incident_id, timestamp, actor, type, message)
SELECT id, TIMESTAMPTZ '2026-10-08 09:14:00+00', 'monitoring', 'ALERT',
       'Consumer group order-events-proc lag above 500k messages on topic order-events (threshold 50k)'
FROM incidents WHERE title = 'Kafka consumer lag after broker restart'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-10-08 09:20:00+00', 'sofia.ramirez', 'ACTION',
       'Confirmed broker kafka-02 restarted at 09:06 during rolling OS patch; partitions rebalanced across remaining brokers'
FROM incidents WHERE title = 'Kafka consumer lag after broker restart'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-10-08 09:41:00+00', 'dev.patel', 'ACTION',
       'Scaled order-events-proc consumers from 12 to 20 instances to drain the backlog faster'
FROM incidents WHERE title = 'Kafka consumer lag after broker restart'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-10-08 10:30:00+00', 'sofia.ramirez', 'NOTE',
       'Lag down to 120k and falling roughly 8k per minute; holding at MITIGATED until the backlog clears'
FROM incidents WHERE title = 'Kafka consumer lag after broker restart';

INSERT INTO log_snippets (incident_id, source, snippet, captured_at)
SELECT id, 'kafka-consumer-groups.sh --describe', $$
GROUP               TOPIC         PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
order-events-proc   order-events  7          88120412        88643109        522697
order-events-proc   order-events  11         87993310        88510244        516934
$$, TIMESTAMPTZ '2026-10-08 09:14:00+00'
FROM incidents WHERE title = 'Kafka consumer lag after broker restart';

INSERT INTO root_causes (incident_id, hypothesis, validated, evidence)
SELECT id,
       'Broker restart triggered a rebalance storm; consumers spent over 20 minutes rejoining before resuming fetch, and the single-threaded deserializer became the bottleneck during catch-up.',
       FALSE,
       'Rebalance metrics show 14 rebalances in 25 minutes; consumer CPU saturated at 98% on deserialization threads during catch-up. Still needs a controlled re-test to confirm.'
FROM incidents WHERE title = 'Kafka consumer lag after broker restart';

-- ---------------------------------------------------------------------------
-- Incident 3: P3 connection pool exhaustion in payments service (RESOLVED)
-- ---------------------------------------------------------------------------
INSERT INTO incidents (title, severity, status, service, started_at, resolved_at, summary)
VALUES (
    'Connection pool exhaustion in payments service',
    'P3', 'RESOLVED', 'payments-api',
    TIMESTAMPTZ '2026-08-30 03:44:00+00',
    TIMESTAMPTZ '2026-08-30 06:10:00+00',
    'The HikariCP web pool on payments-api saturated (50/50 connections in use) because the '
    || 'nightly settlement batch held 38 connections while blocked on a slow fraud-check API. '
    || 'Resolved by killing the runaway batch and splitting batch traffic onto a dedicated pool '
    || 'with a statement timeout.'
);

INSERT INTO timeline_events (incident_id, timestamp, actor, type, message)
SELECT id, TIMESTAMPTZ '2026-08-30 03:46:00+00', 'monitoring', 'ALERT',
       'HikariCP pool exhausted on payments-api (50/50 connections in use for 3 minutes)'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-08-30 03:58:00+00', 'jordan.lee', 'ACTION',
       'Found settlement batch job holding 38 connections while waiting on a slow fraud-check API'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-08-30 04:40:00+00', 'jordan.lee', 'ACTION',
       'Killed the runaway batch run and set a 10s statement timeout on the batch datasource'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service'
UNION ALL
SELECT id, TIMESTAMPTZ '2026-08-30 06:10:00+00', 'jordan.lee', 'NOTE',
       'Pool utilization stable under 40% for 90 minutes. Resolving.'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service';

INSERT INTO log_snippets (incident_id, source, snippet, captured_at)
SELECT id, 'payments-api app log', $$
WARN  c.z.h.p.HikariPool - HikariPool-1 - Connection is not available, request timed out after 30000ms
WARN  c.z.h.p.HikariPool - HikariPool-1 - pool stats (total=50, active=50, idle=0, waiting=27)
$$, TIMESTAMPTZ '2026-08-30 03:46:00+00'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service';

INSERT INTO root_causes (incident_id, hypothesis, validated, evidence)
SELECT id,
       'Nightly settlement batch acquired connections faster than the fraud-check API could respond, starving the web pool; no statement timeout was set on the batch datasource.',
       TRUE,
       'Thread dumps showed 38 batch threads blocked on fraud-check socket reads; after adding a 10s statement timeout and a separate batch pool, peak web-pool usage dropped to 18/50.'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service';

INSERT INTO fixes (incident_id, description, pr_url, commit_sha)
SELECT id,
       'Split batch workload onto a dedicated HikariCP pool (max 15 connections) with a 10s statement timeout, leaving the web pool untouched.',
       'https://github.com/example-org/payments-api/pull/1297',
       'b71e04d9c2a845f0a6d3e19c8f7b2a4d5e6c9012'
FROM incidents WHERE title = 'Connection pool exhaustion in payments service';

-- ---------------------------------------------------------------------------
-- Runbooks
-- ---------------------------------------------------------------------------
INSERT INTO runbooks (title, steps) VALUES ('Redis failover verification', $$
[
  {"order": 1, "title": "Confirm which node is currently primary", "detail": "Query replication info on the expected primary; the role line must read role:master.", "command": "redis-cli -h <primary-host> -p 6379 info replication | grep role"},
  {"order": 2, "title": "Check replica lag", "detail": "master_last_io_seconds_ago above 5 seconds on any replica warrants investigation before proceeding.", "command": "redis-cli -h <replica-host> info replication | grep master_last_io_seconds_ago"},
  {"order": 3, "title": "Verify Sentinel quorum", "detail": "All sentinels must agree on the same master address; a split view means the failover policy is stale.", "command": "redis-cli -h <sentinel-host> -p 26379 sentinel master <master-name>"},
  {"order": 4, "title": "Validate config endpoints against inventory", "detail": "Every cache endpoint referenced in cache-tier.yaml must exist in the inventory; fail the change if any are missing.", "command": "./scripts/check-cache-endpoints.sh --env prod"},
  {"order": 5, "title": "Trigger a controlled failover", "detail": "Run during a low-traffic window and watch client error rates for 5 minutes afterwards.", "command": "redis-cli -h <sentinel-host> -p 26379 sentinel failover <master-name>"},
  {"order": 6, "title": "Confirm client error rates recovered", "detail": "Check the checkout-api dashboard: p99 latency under SLO and Redis timeout errors back to baseline.", "command": null}
]
$$);

INSERT INTO runbooks (title, steps) VALUES ('Kafka consumer lag triage', $$
[
  {"order": 1, "title": "Measure current lag", "detail": "Note the total LAG across partitions; compare against the 50k alert threshold.", "command": "kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group <consumer-group>"},
  {"order": 2, "title": "Check for recent rebalances", "detail": "Frequent rebalances point at unstable consumers or broker restarts rather than a slow consumer.", "command": "kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group <consumer-group> --state"},
  {"order": 3, "title": "Inspect broker health", "detail": "Confirm all brokers are in the ISR set and no partitions are under-replicated.", "command": "kafka-topics.sh --bootstrap-server <broker> --describe --topic <topic> | grep -v \"Isr:.*all\""},
  {"order": 4, "title": "Scale consumers temporarily", "detail": "Add instances up to the partition count to drain the backlog, then scale back.", "command": "kubectl -n order-events scale deployment order-events-proc --replicas=20"},
  {"order": 5, "title": "Look for poison pills or deserialization errors", "detail": "A single bad record can stall a partition if the consumer lacks a dead-letter path.", "command": "kubectl -n order-events logs deploy/order-events-proc --since 30m | grep -i 'deserial\\|SerializationException'"},
  {"order": 6, "title": "Verify lag is draining, then scale back", "detail": "Watch LAG trend to zero over two consecutive describes before restoring the original replica count.", "command": null}
]
$$);

-- Link runbooks to their incidents.
INSERT INTO runbook_incidents (runbook_id, incident_id)
SELECT r.id, i.id
FROM runbooks r, incidents i
WHERE r.title = 'Redis failover verification'
  AND i.title = 'Checkout latency spike - Redis failover misconfig';

INSERT INTO runbook_incidents (runbook_id, incident_id)
SELECT r.id, i.id
FROM runbooks r, incidents i
WHERE r.title = 'Kafka consumer lag triage'
  AND i.title = 'Kafka consumer lag after broker restart';
