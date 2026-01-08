DROP TRIGGER IF EXISTS tr_social_thread_location_bi_point;
DROP TRIGGER IF EXISTS tr_social_thread_location_bu_point;

DELIMITER $$

CREATE TRIGGER tr_social_thread_location_bi_point
    BEFORE INSERT
    ON social_thread_location
    FOR EACH ROW
BEGIN
    -- 1) 经度纬度必须成对出现（publish）
    IF (NEW.publish_longitude IS NULL) <> (NEW.publish_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '发布经度和纬度必须同时为空或同时有值';
    END IF;

    -- 2) 经度纬度必须成对出现（select）
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '选择经度和纬度必须同时为空或同时有值';
    END IF;

    -- 3) 如果要展示定位，必须提供 select 经纬度
    IF NEW.is_show = 1 AND (NEW.select_longitude IS NULL OR NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '展示定位时必须提供选择的经纬度';
    END IF;

    -- 4) 范围校验（可选但推荐）
    IF NEW.publish_longitude IS NOT NULL AND (NEW.publish_longitude < -180 OR NEW.publish_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布经度超出有效范围[-180,180]';
    END IF;
    IF NEW.publish_latitude IS NOT NULL AND (NEW.publish_latitude < -90 OR NEW.publish_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布纬度超出有效范围[-90,90]';
    END IF;

    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择纬度超出有效范围[-90,90]';
    END IF;

    -- 5) 禁止手工写 POINT：永远由经纬度生成
    IF NEW.publish_longitude IS NULL THEN
        SET NEW.publish_location = NULL;
    ELSE
        SET NEW.publish_location = POINT(NEW.publish_longitude, NEW.publish_latitude);
    END IF;

    IF NEW.select_longitude IS NULL THEN
        SET NEW.select_location = NULL;
    ELSE
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$


CREATE TRIGGER tr_social_thread_location_bu_point
    BEFORE UPDATE
    ON social_thread_location
    FOR EACH ROW
BEGIN
    -- 1) 经度纬度必须成对出现（publish）
    IF (NEW.publish_longitude IS NULL) <> (NEW.publish_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '发布经度和纬度必须同时为空或同时有值';
    END IF;

    -- 2) 经度纬度必须成对出现（select）
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '选择经度和纬度必须同时为空或同时有值';
    END IF;

    -- 3) 如果要展示定位，必须提供 select 经纬度
    IF NEW.is_show = 1 AND (NEW.select_longitude IS NULL OR NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '展示定位时必须提供选择的经纬度';
    END IF;

    -- 4) 范围校验
    IF NEW.publish_longitude IS NOT NULL AND (NEW.publish_longitude < -180 OR NEW.publish_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布经度超出有效范围[-180,180]';
    END IF;
    IF NEW.publish_latitude IS NOT NULL AND (NEW.publish_latitude < -90 OR NEW.publish_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布纬度超出有效范围[-90,90]';
    END IF;

    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择纬度超出有效范围[-90,90]';
    END IF;

    -- 5) 强制覆盖 POINT（禁止手工改 POINT）
    IF NEW.publish_longitude IS NULL THEN
        SET NEW.publish_location = NULL;
    ELSE
        SET NEW.publish_location = POINT(NEW.publish_longitude, NEW.publish_latitude);
    END IF;

    IF NEW.select_longitude IS NULL THEN
        SET NEW.select_location = NULL;
    ELSE
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$

DELIMITER ;
