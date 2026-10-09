package com.hospital.dao;

import com.hospital.model.Bed;
import com.hospital.model.Ward;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface WardBedDao {
    List<Ward> findAllWards();
    Optional<Ward> findWardById(int wardId);
    List<Bed> findBedsByWardId(int wardId);
    List<Bed> findAvailableBeds();
    Optional<Bed> findBedById(int bedId);
    boolean setBedOccupied(int bedId, boolean occupied, Connection conn);
    int insertWard(Ward ward);
    int insertBed(Bed bed);
}
