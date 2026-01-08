DROP TRIGGER IF EXISTS tr_social_idle_item_bi_geo;
DROP TRIGGER IF EXISTS tr_social_idle_item_bu_geo;

DELIMITER $$

CREATE TRIGGER tr_social_idle_item_bi_geo
    BEFORE INSERT
    ON social_idle_item
    FOR EACH ROW
BEGIN
    -- 经度纬度必须成对出现
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度必须同时为空或同时有值';
    END IF;

    -- 范围校验（可选但推荐）
    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '纬度超出有效范围[-90,90]';
    END IF;

    -- 强制由经纬度生成 location；未提供坐标则写 POINT(0,0)，并标记 geo_enabled=0
    IF NEW.select_longitude IS NULL THEN
        SET NEW.geo_enabled = 0;
        SET NEW.select_location = POINT(0, 0);
    ELSE
        SET NEW.geo_enabled = 1;
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$

CREATE TRIGGER tr_social_idle_item_bu_geo
    BEFORE UPDATE
    ON social_idle_item
    FOR EACH ROW
BEGIN
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度必须同时为空或同时有值';
    END IF;

    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '纬度超出有效范围[-90,90]';
    END IF;

    -- 禁止手工写 location：永远覆盖
    IF NEW.select_longitude IS NULL THEN
        SET NEW.geo_enabled = 0;
        SET NEW.select_location = POINT(0, 0);
    ELSE
        SET NEW.geo_enabled = 1;
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$

DELIMITER ;
