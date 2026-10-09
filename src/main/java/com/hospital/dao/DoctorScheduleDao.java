package com.hospital.dao;

import com.hospital.model.DoctorSchedule;
import java.util.List;

public interface DoctorScheduleDao {
    List<DoctorSchedule> findByDoctorId(int doctorId);
    int insert(DoctorSchedule schedule);
    boolean delete(int scheduleId);
}
