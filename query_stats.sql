-- Get row counts for all tables
SELECT 
    schemaname,
    relname as tablename,
    n_live_tup as row_count
FROM pg_stat_user_tables
ORDER BY relname;

-- Get indexes
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
