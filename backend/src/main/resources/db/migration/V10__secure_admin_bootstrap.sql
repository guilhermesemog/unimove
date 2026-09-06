-- Public registration is only a one-time bootstrap path for a brand-new installation.
INSERT INTO system_metadata (metadata_key, metadata_value)
SELECT 'admin_bootstrap',
       CASE WHEN EXISTS (SELECT 1 FROM users WHERE role = 'ADMIN') THEN 'claimed' ELSE 'available' END
ON CONFLICT (metadata_key) DO NOTHING;
