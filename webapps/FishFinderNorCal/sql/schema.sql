-- ============================================================
-- FishFinder NorCal — Full MySQL Schema (port 3307)
-- 10 relations in BCNF
-- ============================================================

DROP DATABASE IF EXISTS fishfinder_db;
CREATE DATABASE fishfinder_db;
USE fishfinder_db;

-- -----------------------------------------------------------
-- 1. Users
-- -----------------------------------------------------------
CREATE TABLE Users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  UNIQUE NOT NULL,
    email          VARCHAR(100) UNIQUE NOT NULL,
    password_hash  VARCHAR(64)  NOT NULL,
    account_status ENUM('active', 'suspended', 'deleted') DEFAULT 'active',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 2. Moderators
-- -----------------------------------------------------------
CREATE TABLE Moderators (
    mod_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT UNIQUE NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 3. LocationTypes
-- -----------------------------------------------------------
CREATE TABLE LocationTypes (
    type_id     INT AUTO_INCREMENT PRIMARY KEY,
    type_name   VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 4. Locations
-- -----------------------------------------------------------
CREATE TABLE Locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    latitude    DECIMAL(10, 7),
    longitude   DECIMAL(10, 7),
    region      VARCHAR(100),
    type_id     INT,
    created_by  INT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id)    REFERENCES LocationTypes(type_id),
    FOREIGN KEY (created_by) REFERENCES Users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 5. Species
-- -----------------------------------------------------------
CREATE TABLE Species (
    species_id          INT AUTO_INCREMENT PRIMARY KEY,
    common_name         VARCHAR(100) NOT NULL,
    scientific_name     VARCHAR(150),
    typical_size        VARCHAR(50),
    habitat_description TEXT,
    is_native           BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 6. LocationSpecies (junction)
-- -----------------------------------------------------------
CREATE TABLE LocationSpecies (
    location_id    INT NOT NULL,
    species_id     INT NOT NULL,
    evidence_count INT  DEFAULT 1,
    last_reported  DATE,
    PRIMARY KEY (location_id, species_id),
    FOREIGN KEY (location_id) REFERENCES Locations(location_id),
    FOREIGN KEY (species_id)  REFERENCES Species(species_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 7. CatchReports
-- -----------------------------------------------------------
CREATE TABLE CatchReports (
    report_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT,
    location_id   INT NOT NULL,
    species_id    INT NOT NULL,
    catch_date    DATE NOT NULL,
    weight_lbs    DECIMAL(6, 2),
    length_inches DECIMAL(5, 2),
    method        VARCHAR(100),
    notes         TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES Users(user_id)     ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES Locations(location_id),
    FOREIGN KEY (species_id)  REFERENCES Species(species_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 8. Comments
-- -----------------------------------------------------------
CREATE TABLE Comments (
    comment_id   INT AUTO_INCREMENT PRIMARY KEY,
    report_id    INT,
    user_id      INT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES CatchReports(report_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES Users(user_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 9. Favorites
-- -----------------------------------------------------------
CREATE TABLE Favorites (
    user_id     INT NOT NULL,
    location_id INT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, location_id),
    FOREIGN KEY (user_id)     REFERENCES Users(user_id)         ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES Locations(location_id)  ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- 10. Bans
-- -----------------------------------------------------------
CREATE TABLE Bans (
    ban_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id   INT NOT NULL,
    banned_by INT NOT NULL,
    reason    TEXT NOT NULL,
    ban_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ban_end   DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id)   REFERENCES Users(user_id),
    FOREIGN KEY (banned_by) REFERENCES Users(user_id)
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA
-- ============================================================

-- LocationTypes (7)
INSERT INTO LocationTypes (type_name, description) VALUES
    ('Lake',       'A natural or man-made body of standing freshwater'),
    ('River',      'A large natural flowing watercourse'),
    ('Reservoir',  'An artificial lake used for water storage'),
    ('Creek',      'A small to medium natural stream'),
    ('Bay',        'A broad inlet of the sea or a large lake'),
    ('Ocean/Surf', 'Coastal ocean shoreline and surf zone'),
    ('Delta',      'A wetland area where a river meets a bay or ocean');

-- Species (10 NorCal fish)
INSERT INTO Species (common_name, scientific_name, typical_size, habitat_description, is_native) VALUES
    ('Largemouth Bass',  'Micropterus salmoides',    '2-8 lbs',   'Warm, shallow lakes and ponds with vegetation cover',           FALSE),
    ('Smallmouth Bass',  'Micropterus dolomieu',     '1-5 lbs',   'Cool, clear lakes and rivers with rocky substrate',             FALSE),
    ('Rainbow Trout',    'Oncorhynchus mykiss',      '1-5 lbs',   'Cold, clear streams, rivers, and lakes',                        TRUE),
    ('Brown Trout',      'Salmo trutta',             '1-10 lbs',  'Cold streams and lakes with deep pools and undercut banks',      FALSE),
    ('Chinook Salmon',   'Oncorhynchus tshawytscha', '10-40 lbs', 'Ocean and large rivers; spawns in freshwater tributaries',       TRUE),
    ('Steelhead',        'Oncorhynchus mykiss',      '5-15 lbs',  'Coastal rivers and streams; anadromous rainbow trout',           TRUE),
    ('Striped Bass',     'Morone saxatilis',          '5-30 lbs',  'Bays, deltas, and coastal rivers; brackish to saltwater',        FALSE),
    ('Catfish (Channel)','Ictalurus punctatus',       '2-15 lbs',  'Warm rivers, lakes, and reservoirs with soft bottoms',           FALSE),
    ('Bluegill',         'Lepomis macrochirus',       '0.5-1 lb',  'Warm, shallow ponds and lake edges with vegetation',            FALSE),
    ('Kokanee Salmon',   'Oncorhynchus nerka',        '1-4 lbs',  'Cold, deep lakes and reservoirs; landlocked sockeye salmon',     FALSE);

-- Admin user (password_hash = SHA2('admin123', 256))
INSERT INTO Users (username, email, password_hash, account_status) VALUES
    ('admin', 'admin@fishfinder.com', SHA2('admin123', 256), 'active');

-- Make admin a moderator
INSERT INTO Moderators (user_id) VALUES
    (1);

-- 5 Sample locations (realistic NorCal coordinates)
INSERT INTO Locations (name, description, latitude, longitude, region, type_id, created_by) VALUES
    ('Clear Lake',        'Largest natural freshwater lake entirely within California. Known for world-class bass fishing.',
        39.0060000, -122.7730000, 'Lake County',           1, 1),
    ('Sacramento River',  'Major NorCal river running through the Central Valley. Excellent salmon and steelhead runs.',
        40.5865000, -122.3917000, 'Shasta / Sacramento',   2, 1),
    ('Lake Berryessa',    'Large reservoir in Napa County popular for bass, trout, and kokanee fishing.',
        38.6115000, -122.2534000, 'Napa County',           3, 1),
    ('Putah Creek',       'Scenic creek below Lake Berryessa famous for trophy wild rainbow and brown trout.',
        38.5073000, -121.9710000, 'Yolo / Solano County',  4, 1),
    ('San Francisco Bay', 'Largest estuary on the Pacific Coast. Home to striped bass, halibut, and leopard shark.',
        37.6213000, -122.3790000, 'Bay Area',              5, 1);

-- 5 LocationSpecies entries
INSERT INTO LocationSpecies (location_id, species_id, evidence_count, last_reported) VALUES
    (1, 1, 42, '2026-03-10'),   -- Clear Lake  → Largemouth Bass
    (2, 5, 18, '2026-02-25'),   -- Sacramento River → Chinook Salmon
    (3, 10, 9, '2026-03-05'),   -- Lake Berryessa → Kokanee Salmon
    (4, 3, 27, '2026-03-12'),   -- Putah Creek → Rainbow Trout
    (5, 7, 34, '2026-01-30');   -- SF Bay → Striped Bass

-- 3 Sample catch reports from admin (user_id = 1)
INSERT INTO CatchReports (user_id, location_id, species_id, catch_date, weight_lbs, length_inches, method, notes) VALUES
    (1, 1, 1, '2026-03-10', 5.75, 21.00, 'Plastic worm, Texas rig', 'Caught near tule beds on the west shore. Great fight!'),
    (1, 2, 5, '2026-02-25', 22.30, 36.50, 'Salmon roe under float',  'Fall-run Chinook near Redding. Bright chrome fish.'),
    (1, 4, 3, '2026-03-12', 3.10, 18.25, 'Dry fly – Blue Winged Olive', 'Wild rainbow in the catch-and-release section below Monticello Dam.');

-- 2 Sample comments
INSERT INTO Comments (report_id, user_id, comment_text) VALUES
    (1, 1, 'Clear Lake never disappoints for bass. Tight lines everyone!'),
    (2, 1, 'That is a beautiful Chinook. What pound test leader were you using?');
