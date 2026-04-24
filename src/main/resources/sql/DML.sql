USE xy;

INSERT INTO Dot (id, name, x, y)
WITH RECURSIVE seq AS (SELECT 1 AS n
                       UNION ALL
                       SELECT n + 1
                       FROM seq
                       WHERE n < 100)
SELECT UUID(),
       CONCAT('D_', n),
       ROUND(RAND() * 100, 2),
       ROUND(RAND() * 100, 2)
FROM seq;