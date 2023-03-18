package com.example.crio.cred.Utils;

import lombok.experimental.UtilityClass;
import java.time.Month;
import com.example.crio.cred.exceptions.MonthYearInvalidException;

@UtilityClass
public class Validation {
    
    public Boolean validateMonthAndYear(String month, String year){
        try{
            Month.of(Integer.parseInt(month));
            Integer.parseInt(year);
            String monthYear = month + "/" + year;
            if(Utils.differenceInDays(monthYear) > 0){
                throw new MonthYearInvalidException(Constants.MONTH_YEAR_INVALID);
            }
        }catch(Exception e){
            return false;
        }
        return true;
    }

    public Boolean validateAmount(Double amt){
        return amt > 0.0;
    }
}
