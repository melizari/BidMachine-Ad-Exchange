#---!Ups
CREATE TABLE sellers(
  id BIGSERIAL PRIMARY KEY,
  ks VARCHAR(255),
  name VARCHAR(255),
  fee DOUBLE PRECISION,
  active BOOLEAN,
  blocked_categories VARCHAR(16)[],
  blocked_advertisers VARCHAR(255)[],
  blocked_apps VARCHAR(255)[],
  CONSTRAINT ks_unique UNIQUE (ks)
);


CREATE TABLE apps(
  id BIGSERIAL PRIMARY KEY,
  eid VARCHAR(255),
  ks VARCHAR(255),
  seller_id BIGINT REFERENCES sellers ON DELETE CASCADE ON UPDATE RESTRICT NOT NULL,
  platform VARCHAR(16) NOT NULL,
  name VARCHAR(255),
  bundle VARCHAR(255),
  domain VARCHAR(255),
  storeurl VARCHAR(1024),
  storeid VARCHAR(255),
  privacypolicy BOOLEAN,
  paid BOOLEAN,
  publisher_id VARCHAR(255),
  publisher_name VARCHAR(255),
  publisher_cat VARCHAR(16)[],
  publisher_domain VARCHAR(255),
  keywords VARCHAR(255),
  settings_blocked_bidders VARCHAR(36)[],
  settings_blocked_categories VARCHAR(16)[],
  settings_blocked_advertisers VARCHAR(255)[],
  cat VARCHAR(16)[],
  CONSTRAINT apps_ks_eid_idx UNIQUE (ks, eid)
);

CREATE TABLE ad_unit_configs(
  id BIGSERIAL PRIMARY KEY,
  eid VARCHAR(255),
  ks VARCHAR(255),
  app_id BIGINT REFERENCES apps ON DELETE CASCADE ON UPDATE RESTRICT NOT NULL,
  demand_partner_code VARCHAR(36) NOT NULL,
  ad_type VARCHAR(16) NOT NULL,
  format jsonb[],
  is_interstitial BOOLEAN,
  is_rewarded BOOLEAN,
  custom_params jsonb,
  CONSTRAINT ad_unit_configs_ks_eid_idx UNIQUE (ks, eid)
);

#---!Downs
DROP TABLE ad_unit_configs;
DROP TABLE apps;
DROP TABLE sellers;
