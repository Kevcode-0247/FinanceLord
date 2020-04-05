package protect.FinanceLord.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface AssetsValueDao {

    @Insert
    void insertAssetValue(AssetsValue assetsValue);

    @Update
    void updateAssetValue(AssetsValue ... assetsValue);

    @Query("SELECT * FROM AssetsValue WHERE assetsId LIKE :assetId")
    List<AssetsValue> queryAssetsById  (int assetId);

    @Query("SELECT * FROM ASSETSVALUE WHERE date >= :date")
    List<AssetsValue> queryAssetsSinceDate(Long date);

    @Query("SELECT * FROM AssetsValue")
    List<AssetsValue> queryAllAssetsValue();

    @Query("SELECT * FROM ASSETSVALUE WHERE date = :date and assetsId = :assetId")
    List<AssetsValue> queryAsset(int assetId, Long date);
}
