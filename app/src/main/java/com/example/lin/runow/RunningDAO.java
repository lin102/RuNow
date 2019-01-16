package com.example.lin.runow;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao
public interface RunningDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Runningdata runningdata);

    @Update
    public void update(Runningdata runningdata);

    @Delete
    public void delete(Runningdata runningdata);

    @Query("SELECT * FROM running_table")
    public List<Runningdata> getAllRuningdata();

   /* @Query("SELECT * FROM running_table WHERE test = :running_test")
    public List<Runningdata> getRunningInfoBytest(int running_test);*/

}