package com.coathar.framework.csvconverter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.sql.Timestamp;

public class TimestampConverter extends AbstractBeanField
{
    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException
    {
        return Timestamp.valueOf(value + " 00:00:00");
    }
}
