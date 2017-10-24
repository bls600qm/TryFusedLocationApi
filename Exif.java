package jp.rika.sumitomo.tryfusedlocationapi;

/**
 * Created by rikasumitomo on 2017/10/24.
 */

public class Exif {

    //以下Exif情報の取得変換 ****************************************************

       public double ExifHourMinSecToDegreesLatitude(String latitudE) {
              String hourminsec[] = latitudE.split(",");
              String hour[] = hourminsec[0].split("/");
              String min[] = hourminsec[1].split("/");
              String sec[] = hourminsec[2].split("/");
              double dhour = (double)Integer.parseInt(hour[0]) / (double)Integer.parseInt(hour[1]);
              double dmin = (double)Integer.parseInt(min[0]) / (double)Integer.parseInt(min[1]);
              double dsec = (double)Integer.parseInt(sec[0]) / (double)Integer.parseInt(sec[1]);
              double degrees = dhour + dmin / 60.0 + dsec / 3600.0;
              return degrees;
       }
       public double ExifHourMinSecToDegreesLongitude(String longitudE) {
              String hourminsec[] = longitudE.split(",");
              String hour[] = hourminsec[0].split("/");
              String min[] = hourminsec[1].split("/");
              String sec[] = hourminsec[2].split("/");
              double dhour = (double)Integer.parseInt(hour[0]) / (double)Integer.parseInt(hour[1]);
              double dmin = (double)Integer.parseInt(min[0]) / (double)Integer.parseInt(min[1]);
              double dsec = (double)Integer.parseInt(sec[0]) / (double)Integer.parseInt(sec[1]);
              double degrees = dhour + dmin / 60.0 + dsec / 3600.0;
              return degrees;
       }

       public String ExifLatitudeToDegrees(String ref, String latitudE) {
              String answer = String.valueOf(ref.equals("S") ? -1.0 : 1.0 * ExifHourMinSecToDegreesLatitude(latitudE));
              return answer;
       }

       public String ExifLongitudeToDegrees(String ref, String longitudE) {
             String answer = String.valueOf(ref.equals("W") ? -1.0 : 1.0 * ExifHourMinSecToDegreesLongitude(longitudE));
             return answer;
       }

}
