#---!Ups
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE abstract_rtb_app(
  rtb_app_id TEXT,
  rtb_app_name TEXT,
  rtb_app_bundle TEXT,
  rtb_app_domain TEXT,
  rtb_app_store_url TEXT,
  rtb_app_cat TEXT[],
  rtb_app_section_cat TEXT[],
  rtb_app_page_cat TEXT[],
  rtb_app_ver TEXT,
  rtb_app_privacy_policy BOOLEAN,
  rtb_app_paid BOOLEAN,
  rtb_app_keywords TEXT,
  rtb_app_ext JSONB
);

CREATE TABLE abstract_rtb_publisher(
  rtb_publisher_id TEXT,
  rtb_publisher_name TEXT,
  rtb_publisher_cat TEXT[],
  rtb_publisher_domain TEXT,
  rbt_publisher_ext JSONB
);

CREATE TABLE abstract_rtb_banner(
  rtb_banner_w INT,
  rtb_banner_h INT,
  rtb_banner_btype INT[],
  rtb_banner_battr INT[],
  rtb_banner_pos INT,
  rtb_banner_mimes TEXT[],
  rtb_banner_topframe BOOLEAN,
  rtb_banner_expdir INT[],
  rtb_banner_api INT[],
  rtb_banner_ext JSONB
);

CREATE TABLE abstract_rtb_video(
  rtb_video_mimes TEXT[],
  rtb_video_minduration INT,
  rtb_video_maxduration INT,
  rtb_video_protocol INT,
  rtb_video_protocols INT[],
  rtb_video_w INT,
  rtb_video_h INT,
  rtb_video_startdelay INT,
  rtb_video_linearity INT,
  rtb_video_battr INT[],
  rtb_video_maxextended INT,
  rtb_video_minbitrate INT,
  rtb_video_maxbitrate INT,
  rtb_video_boxingallowed BOOLEAN DEFAULT TRUE,
  rtb_video_playbackmethod INT[],
  rtb_video_delivery INT[],
  rtb_video_pos INT,
  rtb_video_api INT[],
  rtb_video_ext JSONB
);


CREATE TABLE agency(
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  ext_id INT,
  active BOOLEAN,
  contact_name TEXT,
  instant_messaging TEXT,
  phone TEXT,
  email TEXT,
  site TEXT
);

CREATE TABLE bidder(
  id BIGSERIAL PRIMARY KEY,
  agency_id BIGINT REFERENCES agency ON DELETE CASCADE ON UPDATE RESTRICT NOT NULL,
  title TEXT NOT NULL,
  endpoint TEXT NOT NULL,
  rtb_version INT NOT NULL,
  coppa_flag BOOLEAN NOT NULL,

  worldwide BOOLEAN NOT NULL,
  countries TEXT[],
  platforms TEXT[] NOT NULL,

  max_rpm INT NOT NULL,


  excluded_sellers BIGINT[],
  included_sellers BIGINT[],
  protocol TEXT DEFAULT 'openrtb',
  auction_type INT DEFAULT 2,
  ad_control BOOLEAN DEFAULT FALSE
);

CREATE TABLE publisher(
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  auto_app_creation BOOLEAN DEFAULT FALSE
);


CREATE TABLE app(
  id BIGSERIAL PRIMARY KEY,
  extrnal_id TEXT,
  publisher_id BIGINT REFERENCES publisher ON DELETE CASCADE ON UPDATE RESTRICT NOT NULL

) INHERITS (abstract_rtb_app, abstract_rtb_publisher);

CREATE TABLE abstract_ad_space(
  id BIGSERIAL PRIMARY KEY,
  seller_id BIGINT,
  interstitial BOOLEAN NOT NULL DEFAULT FALSE,
  debug BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  title TEXT,
  display_manager TEXT,
  ad_channel INT,
  distribution_channel VARCHAR(16),
  reward BOOLEAN DEFAULT FALSE
);

CREATE TABLE banner_ad_space() INHERITS (abstract_ad_space, abstract_rtb_banner);
CREATE TABLE video_ad_space() INHERITS (abstract_ad_space, abstract_rtb_video);

CREATE TABLE abstract_ad_profile(
  id BIGSERIAL PRIMARY KEY,
  bidder_id BIGINT REFERENCES bidder ON DELETE CASCADE ON UPDATE RESTRICT,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  interstitial BOOLEAN NOT NULL DEFAULT FALSE,
  debug BOOLEAN NOT NULL DEFAULT FALSE,
  delayed_notification BOOLEAN NOT NULL DEFAULT TRUE,
  title TEXT,
  ad_channel INT,
  dm_ver_max TEXT,
  dm_ver_min TEXT,
  distribution_channel VARCHAR(16),
  template TEXT,
  reward BOOLEAN DEFAULT FALSE,
  allow_cache BOOLEAN DEFAULT TRUE,
  allow_close_delay INT DEFAULT 0
);

CREATE TABLE banner_ad_profile() INHERITS (abstract_ad_profile, abstract_rtb_banner);
CREATE TABLE video_ad_profile() INHERITS (abstract_ad_profile, abstract_rtb_video);

CREATE TABLE abstract_rtb_native(
  rtb_native_ver TEXT,
  rtb_native_api INT[],
  rtb_native_battr INT[],
  rtb_native_ext JSONB
);

CREATE TABLE native_ad_space(
    rtb_native_request TEXT
) INHERITS (abstract_ad_space, abstract_rtb_native);

CREATE TABLE native_ad_profile(
    rtb_native_request TEXT
) INHERITS (abstract_ad_profile, abstract_rtb_native);

CREATE TABLE exchange_global_settings(
  banner_exchange_fee INT,
  video_exchange_fee INT,
  native_exchange_fee INT,
  interstitial_exchange_fee INT,
  t_max INT,
  debug_bid_response_topic TEXT,
  debug_is_interstitial BOOLEAN DEFAULT FALSE,
  debug_is_banner BOOLEAN DEFAULT FALSE,
  debug_is_video BOOLEAN DEFAULT FALSE,
  debug_is_native BOOLEAN DEFAULT FALSE,
  debug_app_id INT DEFAULT -1,
  debug_bidder_id INT DEFAULT -1,
  debug_only_bid BOOLEAN DEFAULT TRUE,
  debug_with_adm BOOLEAN DEFAULT FALSE,
  debug_force_no_fill BOOLEAN DEFAULT FALSE
);

#---!Downs
DROP TABLE banner_ad_profile;
DROP TABLE video_ad_profile;
DROP TABLE abstract_ad_profile;

DROP TABLE banner_ad_space;
DROP TABLE video_ad_space;
DROP TABLE abstract_ad_space;

DROP TABLE app;
DROP TABLE publisher;
DROP TABLE bidder;
DROP TABLE agency;

DROP TABLE abstract_rtb_video;
DROP TABLE abstract_rtb_banner;
DROP TABLE abstract_rtb_publisher;
DROP TABLE abstract_rtb_app;

DROP EXTENSION IF EXISTS hstore;