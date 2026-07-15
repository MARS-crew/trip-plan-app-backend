-- Run this once against the target MySQL database before deploying the application.
-- It keeps the latest active place for each Google Place ID, moves every FK to it,
-- removes duplicate link rows, and finally enforces uniqueness at the database level.

USE trip;

SET @previous_sql_safe_updates = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

START TRANSACTION;

UPDATE place
SET google_place_id = NULL
WHERE google_place_id IS NOT NULL
  AND TRIM(google_place_id) = '';

DROP TEMPORARY TABLE IF EXISTS place_google_duplicate_map;
CREATE TEMPORARY TABLE place_google_duplicate_map (
    duplicate_place_id BIGINT NOT NULL PRIMARY KEY,
    canonical_place_id BIGINT NOT NULL,
    google_place_id VARCHAR(120) NOT NULL
);

INSERT INTO place_google_duplicate_map (duplicate_place_id, canonical_place_id, google_place_id)
SELECT place_id, canonical_place_id, google_place_id
FROM (
    SELECT p.place_id,
           p.google_place_id,
           FIRST_VALUE(p.place_id) OVER (
               PARTITION BY p.google_place_id
               ORDER BY p.is_deleted ASC,
                        p.updated_at DESC,
                        p.created_at DESC,
                        p.place_id DESC
           ) AS canonical_place_id,
           ROW_NUMBER() OVER (
               PARTITION BY p.google_place_id
               ORDER BY p.is_deleted ASC,
                        p.updated_at DESC,
                        p.created_at DESC,
                        p.place_id DESC
           ) AS duplicate_rank
    FROM place p
    WHERE p.google_place_id IS NOT NULL
) ranked
WHERE duplicate_rank > 1;

UPDATE review child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

UPDATE saved_place child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

UPDATE wishlist_place child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

UPDATE visited_place child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

UPDATE trip_schedule child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

UPDATE place_tag_map child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

UPDATE search_cache_place child
JOIN place_google_duplicate_map duplicate_map
  ON child.place_id = duplicate_map.duplicate_place_id
SET child.place_id = duplicate_map.canonical_place_id;

DELETE duplicate_link
FROM place_tag_map duplicate_link
JOIN place_tag_map canonical_link
  ON canonical_link.place_id = duplicate_link.place_id
 AND canonical_link.place_tag_id = duplicate_link.place_tag_id
 AND canonical_link.place_tag_map_id < duplicate_link.place_tag_map_id;

DELETE duplicate_link
FROM search_cache_place duplicate_link
JOIN search_cache_place canonical_link
  ON canonical_link.search_cache_id = duplicate_link.search_cache_id
 AND canonical_link.place_id = duplicate_link.place_id
 AND canonical_link.search_cache_place_id < duplicate_link.search_cache_place_id;

DELETE duplicate_place
FROM place duplicate_place
JOIN place_google_duplicate_map duplicate_map
  ON duplicate_place.place_id = duplicate_map.duplicate_place_id;

COMMIT;

SET @unique_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'place'
      AND index_name = 'uk_place_google_place_id'
);
SET @create_unique_index_sql = IF(
    @unique_index_exists = 0,
    'CREATE UNIQUE INDEX uk_place_google_place_id ON place (google_place_id)',
    'SELECT 1'
);
PREPARE create_unique_index_statement FROM @create_unique_index_sql;
EXECUTE create_unique_index_statement;
DEALLOCATE PREPARE create_unique_index_statement;

SELECT google_place_id, COUNT(*) AS duplicate_count
FROM place
WHERE google_place_id IS NOT NULL
GROUP BY google_place_id
HAVING COUNT(*) > 1;

SET SQL_SAFE_UPDATES = @previous_sql_safe_updates;
