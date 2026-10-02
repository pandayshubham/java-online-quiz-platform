package com.quizplatform.dao;

import com.quizplatform.model.SystemSetting;
import java.util.List;

/**
 * Data Access Object interface for SystemSetting entities.
 */
public interface SystemSettingDAO {

    SystemSetting findByKey(String key);

    List<SystemSetting> findAll();

    boolean updateSetting(SystemSetting setting);

    boolean existsByKey(String key);

    int createSetting(SystemSetting setting);

    boolean saveOrUpdate(SystemSetting setting);
}
