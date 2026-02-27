-- V4: Seed initial job control rows
-- jobStatusID: 0=Pending, 1=Success, 2=Failed
-- nextJobAt set to NOW() so each job is eligible to run immediately on first startup

INSERT INTO ralysis.jobinfos (jobTypeID, jobValue, jobStatusID, nextJobAt, createdAt, updatedAt)
SELECT 1, CURRENT_DATE - 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ralysis.jobinfos WHERE jobTypeID = 1);

INSERT INTO ralysis.jobinfos (jobTypeID, jobValue, jobStatusID, nextJobAt, createdAt, updatedAt)
SELECT 2, CURRENT_DATE - 1, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ralysis.jobinfos WHERE jobTypeID = 2);

INSERT INTO ralysis.jobinfos (jobTypeID, jobValue, jobStatusID, nextJobAt, createdAt, updatedAt)
SELECT 3, CURRENT_DATE, 1, NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ralysis.jobinfos WHERE jobTypeID = 3);
