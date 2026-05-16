ALTER TABLE ad_block DROP CONSTRAINT chk_ad_block_dates;
ALTER TABLE ad_block ADD CONSTRAINT chk_ad_block_dates CHECK (end_date >= start_date);