--  写入时自动生成 location

DELIMITER $$

CREATE TRIGGER `tr_core_school_campus_bi_location`
    BEFORE INSERT
    ON `core_school_campus`
    FOR EACH ROW
BEGIN
    -- 经纬度必须有值（否则无法生成 location）
    IF NEW.longitude IS NULL OR NEW.latitude IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度不能为空';
    END IF;

    -- 基本范围校验（经度[-180,180] 纬度[-90,90]）
    IF NEW.longitude < -180 OR NEW.longitude > 180
        OR NEW.latitude < -90 OR NEW.latitude > 90 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经纬度超出有效范围（经度[-180,180]，纬度[-90,90]）';
    END IF;

    -- 禁止手工写 location：永远以经纬度生成
    SET NEW.location = POINT(NEW.longitude, NEW.latitude);
END$$


CREATE TRIGGER `tr_core_school_campus_bu_location`
    BEFORE UPDATE
    ON `core_school_campus`
    FOR EACH ROW
BEGIN
    -- 经纬度必须有值（否则无法生成 location）
    IF NEW.longitude IS NULL OR NEW.latitude IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度不能为空';
    END IF;

    -- 基本范围校验（经度[-180,180] 纬度[-90,90]）
    IF NEW.longitude < -180 OR NEW.longitude > 180
        OR NEW.latitude < -90 OR NEW.latitude > 90 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经纬度超出有效范围（经度[-180,180]，纬度[-90,90]）';
    END IF;

    -- 无论用户是否手工修改 location，都强制覆盖：永远以经纬度为准
    SET NEW.location = POINT(NEW.longitude, NEW.latitude);
END$$

DELIMITER ;
