package com.coathar.loaders;

import com.coathar.Utils;
import com.coathar.framework.Loader;
import com.coathar.framework.csvconverter.TimestampConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

public class SeasonLoader extends Loader<SeasonLoader.Season>
{
    @Override
    protected boolean checkHash(Season file, Season cached)
    {
        int fileHash = Utils.generateHash(file.SeasonNumber, file.StartDate, file.EndDate);
        int cachedHash = Utils.generateHash(cached.SeasonNumber, cached.StartDate, cached.EndDate);

        return fileHash != cachedHash;
    }

    @Override
    protected String getSchema()
    {
        return "GazeboCore";
    }

    @Data
    @NoArgsConstructor
    protected static class Season
    {
        @CsvBindByName
        private String GazeboID;

        @CsvBindByName
        private int SeasonNumber;

        @CsvCustomBindByName(converter = TimestampConverter.class)
        private Timestamp StartDate;

        @CsvCustomBindByName(converter = TimestampConverter.class)
        private Timestamp EndDate;

        @Override
        public String toString()
        {
            return GazeboID + "," +
                    SeasonNumber + "," +
                    StartDate + "," +
                    EndDate;
        }
    }
}
