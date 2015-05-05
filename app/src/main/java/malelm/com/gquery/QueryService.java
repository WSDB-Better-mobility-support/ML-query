    package malelm.com.gquery;

    import android.app.Service;
    import android.content.Intent;
    import android.location.Location;
    import android.os.IBinder;
    import android.util.Log;

    import java.io.BufferedOutputStream;
    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;

    import malelm.com.gquery.dataModel.LocationUnit;
    import malelm.com.gquery.dataModel.SELatLon;
    import malelm.com.gquery.db.LocationDataSource;

    public class QueryService extends Service {

        private static final String TAG = QueryService.class.getSimpleName();
        public static final int TWO_LOC_CONTROL = 1;
        public static final double SPEED_CONTROL = 1;
        public static final int GOOGLE_AREA = 100;
        public static final int NUM_OF_LOC_CONTROL = 0;
        public static final double DIF_LL_100_M = 0.001;
        public static final int LAT_SHIFT = -12;
        public static final int LON_SHIFT = -100;
        public static final double DIR_CONTROL = 0.15;
        private LocationDataSource lDSource;
        private boolean firstQuery = true, secondQuery = false;
        private double numOfLoc = 1;
        private long timeBefore;
        private long timeAfter;
        private SELatLon seLatLon;
        private ArrayList<Double> speed, latArr, lonArr;

        private double lastLat;
        private double lastLon;
        private int counter;

        public QueryService() {
            Log.d(TAG, "QueryService");
        }

        @Override
        public void onCreate() {
            super.onCreate();
            Log.d(TAG, "onCreate");
            lDSource = new LocationDataSource(this);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            counter++;
            List<LocationUnit> locations;
            // The first query has a fixed params and it is used to prepare for the next query
            if (firstQuery) {
                currentLocationQuery("FIRST STAGE");
            }
            // In the second query we need to find out the size of the query and its direction
            else if (secondQuery) {
                writeToFile("query.txt", "SECOND QUERY\n");
                timeBeforeAfter();
                writeToFile("query.txt", "Time:"+ timeBefore+"\n"+timeAfter+"\n");
                lDSource.open();
                // Getting the data within the previous query time
                // If there is an error in timing do the current location query and return
                if(timeAfter <= 0 || timeBefore <= 0 || (timeAfter <= timeBefore) ){
                    currentLocationQuery("SECOND: TIME ERROR");
                    return super.onStartCommand(intent, flags, startId);
                }
                locations = lDSource.findData(timeBefore, timeAfter);
                lDSource.close();
                // To check if there was some relevant data in the database
                if (locations.size() < TWO_LOC_CONTROL) {
                    // if there is not relevant location data do one location query which is your current location
                    currentLocationQuery("NOT ENOUGH LOCATIONS");
                } else {
                    if (locations.size() == 1){ // if there is only one object pull one extra object before the query time
                                                // this approach might cause a problem in the direction change detection
                        locations.add( lDSource.oneExtraLoc(timeBefore));
                    }

                    // SPEED TEST an calculating the numOfLoc
                    double aveSpeed = getAveSpeed(locations);
                    int MaxNumOfLoc =  getMaxNumOfLoc( aveSpeed);

                    if (aveSpeed <= SPEED_CONTROL) {
                        numOfLoc = MaxNumOfLoc;
                    }else {   // this (if) is to protect from the case where the speed is approximating 0

                        double time = ((timeAfter - timeBefore) - GoogleSpectrumQuery.sleepTime) / 1000; // Time in seconds and the superimpose sleeping time is subtracted
                        double distOfQuery = Math.floor(aveSpeed * time);
                        // numOfLoc
                        numOfLoc = Math.floor((GOOGLE_AREA * numOfLoc - distOfQuery) / distOfQuery);

                        if (numOfLoc <= NUM_OF_LOC_CONTROL) {
                            currentLocationQuery("NUM OF LOCATIONS IS " + numOfLoc + "SECOND STAGE");
                            // Stop and return
                            return super.onStartCommand(intent, flags, startId); // important to not allow  "secondQuery = false" to be executed
                        } else if (numOfLoc > MaxNumOfLoc) {
                            numOfLoc = MaxNumOfLoc;
                        }
                    }
                    // DIRECTION
                    double[] changeFactors = getChangeFactors(locations);
                    double lat = latArr.get(0); // This is you last know location within the query time
                    double lon = lonArr.get(0);
                    // DO the Query
                    writeToFile("query.txt" , "lat = " + lat  +"\nlon = "+ lon  +
                            "\nlatChFac = "+changeFactors[0]+"\nlonChFac = " +
                            changeFactors[1] + "\nnumOfLoc = "+ numOfLoc+"\n\n");
                    GoogleSpectrumQuery.query(lat + LAT_SHIFT, lon + LON_SHIFT, changeFactors[0], changeFactors[1], numOfLoc);
                    writeToFile("path.txt" , counter,lat , lon , changeFactors[0],changeFactors[1], numOfLoc);
                    // Calculating where you reached with your query for the next query
                    lastLat = numOfLoc * DIF_LL_100_M * changeFactors[0] + lat ;
                    lastLon = numOfLoc * DIF_LL_100_M * changeFactors[1] + lon ;
                    // reset the MaxNumOfloc

                secondQuery = false;
                }
            } else {
                //FROM THE THIRD AND THE NEXT QUERIES we need to find out the size of the query and the direction and compare the current direction to the previous one
                // to find out from were to start your location
                // Then we need to find out what is the left distance and if we have to make a query or not
                writeToFile("query.txt", "THIRD QUERY\n");

                //DIRECTION TEST
                // initialize the fields timeBefore/timeAfter for the last query
                timeBeforeAfter();
                writeToFile("query.txt", "Time:"+(timeAfter-timeBefore)+"\n"+  timeBefore+"\n"+timeAfter+"\n");
                lDSource.open();
                // Getting the data within the previous query time
                locations = lDSource.findData(timeBefore, timeAfter);
                // get the location objects after the last query
                lDSource.close();
                // get the location objects after the last query
                List<LocationUnit> loc = getAfterQueryLocations();
                // If there is an error in timing do the current location query and return
                if(timeAfter <= 0 || timeBefore <= 0 || (timeAfter <= timeBefore) ){
                    currentLocationQuery("THIRD: TIME ERROR");
                    return super.onStartCommand(intent, flags, startId);
                }
                // To check if there was some relevant data in the database
                if (locations.size() < TWO_LOC_CONTROL) {
                    // if there is not relevant location data do one location query which is your current location
                         currentLocationQuery("NOT ENOUGH LOCATIONS");
                         // Stop and return

                } else {
                    if (locations.size() == 1){
                        locations.add( lDSource.oneExtraLoc(timeBefore));
                    }
                    double[] changeFactors = getChangeFactors(locations);
                    // At this stage we should have a direction after the last query and we can compare them
                    double[] NewchangeFactors = getChangeFactors(loc);

                    // TODO 0.15 is an arbitrary value, I need to find a proper value
                    if (Math.abs(NewchangeFactors[0] - changeFactors[0]) > DIR_CONTROL || Math.abs(NewchangeFactors[1] - changeFactors[1]) > DIR_CONTROL) {
                        //The direction has been change Quick recovery
                        currentLocationQuery("DIRECTION CHANGE");
                        // stop and return
                        return super.onStartCommand(intent, flags, startId);
                    } else {
                        //Build On top of the previous query
                        // SPEED TEST
                        double aveSpeed = getAveSpeed(locations);
                        int MaxNumOfLoc =  getMaxNumOfLoc( aveSpeed);
                        if (aveSpeed <= SPEED_CONTROL) {
                            numOfLoc = MaxNumOfLoc;
                        } else {   // this (if) is to protect from the case where the speed is approximating 0
                            double time = ((timeAfter - timeBefore) - GoogleSpectrumQuery.sleepTime) / 1000; // Time in seconds and the superimpose sleeping time is subtracted
                            double distOfQuery = Math.floor(aveSpeed * time);
                            numOfLoc = Math.floor((GOOGLE_AREA * numOfLoc - distOfQuery) / distOfQuery);
                            if (numOfLoc <= NUM_OF_LOC_CONTROL) {
                                currentLocationQuery("NUM OF LOCATIONS IS "+ numOfLoc+"SECOND STAGE");
                                // Stop and return
                                return super.onStartCommand(intent, flags, startId); // important to not allow  "secondQuery = false" to be executed
                            } else if (numOfLoc > MaxNumOfLoc) {
                                numOfLoc = MaxNumOfLoc;
                            }
                        }

                        lDSource.open();
                        LocationUnit l = lDSource.findFirstRow(); // Last known location, current location
                        lDSource.close();
                        float[] res = new float[10];
                        //Find the distance between where you reached with your last query and your current distance
                        Location currentLocation = new Location("pointA");
                        currentLocation.setLatitude(l.getLat());
                        currentLocation.setLongitude(l.getLon());

                        Location lastLocation = new Location("pointB");
                        lastLocation.setLatitude(lastLat);
                        lastLocation.setLongitude(lastLon);
                        double leftDis = currentLocation.distanceTo(lastLocation);

                        double leftNumOfloc = Math.floor(leftDis / GOOGLE_AREA);

                        if (leftNumOfloc <= MaxNumOfLoc/2) {
                            writeToFile("query.txt" , "lat = " + lastLat  +"\nlon = "+ lastLon +
                                    "\nlatChFac = "+changeFactors[0]+"\nlonChFac = " +
                                    changeFactors[1] + "\nnumOfLoc = "+ numOfLoc + "\n\n");
                            GoogleSpectrumQuery.query(lastLat + LAT_SHIFT, lastLon + LON_SHIFT, changeFactors[0], changeFactors[1], numOfLoc);
                            writeToFile("path.txt" , counter,lastLat , lastLon , changeFactors[0],changeFactors[1], numOfLoc);
                            lastLat = numOfLoc * DIF_LL_100_M * changeFactors[0] + lastLat ;
                            lastLon = numOfLoc * DIF_LL_100_M * changeFactors[1] + lastLon ;
                            // prepare the lastLat/lastLon for the next query
                        }
                    }
                }

            }
            return super.onStartCommand(intent, flags, startId);
        }

        private int getMaxNumOfLoc( double aveSpeed) {
            int maxNumOfLoc = 5;
            if (aveSpeed > 14 && aveSpeed < 16) {
                maxNumOfLoc = 7;
            } else if (aveSpeed > 16 && aveSpeed < 20) {
                maxNumOfLoc = 10;
            } else if (aveSpeed > 20 && aveSpeed < 27) {
                maxNumOfLoc = 15;
            } else if(aveSpeed > 27) {
                maxNumOfLoc =20;
            }
            return maxNumOfLoc;
        }

        private List<LocationUnit> getAfterQueryLocations() {
            lDSource.open();
            // Getting the data after the previous query time
            List<LocationUnit> locations = lDSource.findData(timeAfter);
            lDSource.close();

            // The locations are less then 2 sleep for a second and check again
            while (locations.size() < TWO_LOC_CONTROL) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lDSource.open();
                // Getting the data after the previous query time
                locations = lDSource.findData(timeAfter);
                lDSource.close();
            }
            return locations;
        }


        private double[] getChangeFactors(List<LocationUnit> locations) {
            lonArr = new ArrayList<>();
            latArr = new ArrayList<>();
            for (LocationUnit locat : locations) {
                latArr.add(locat.getLat());
                lonArr.add(locat.getLon());
            }
            //the lat/long that the query will start from
            // Get the first locations if there is a change in direction

            double aveLatChange = 0;
            double aveLonChange = 0;
            double latChangeFactor = 1;
            double lonChangeFactor = 1;
            for (int i = 0; i < latArr.size() - 1; i++) {
                aveLatChange +=  latArr.get(i)   -  latArr.get(i + 1 ) ; // First entry in the list is the most recent
                aveLonChange +=  lonArr.get(i) -  lonArr.get(i + 1)  ;
            }

            aveLatChange = aveLatChange / ( latArr.size() -1 ) ; //averaging the differences. They are less than the array by 1
            aveLonChange = aveLonChange / ( lonArr.size() -1 ) ;
            latChangeFactor = aveLatChange / (Math.abs(aveLatChange) + Math.abs(aveLonChange));
            lonChangeFactor = aveLonChange / (Math.abs(aveLatChange) + Math.abs(aveLonChange));

            double[] res = {latChangeFactor, lonChangeFactor};
            return res;
        }

        private double getAveSpeed(List<LocationUnit> locations) {
            double sumSpeed = 0;
            speed = new ArrayList<>();
            for (LocationUnit locat : locations) {
                speed.add(locat.getSpeed());
                sumSpeed += locat.getSpeed();
            }
            double aveSpeed = sumSpeed / speed.size()-1;
            return aveSpeed;
        }

        private void timeBeforeAfter() {
            timeBefore = GoogleSpectrumQuery.timeBeforeQuery;
            timeAfter = GoogleSpectrumQuery.timeAfterQuery;
        }

        private void currentLocationQuery(String mes) {
            writeToFile("query.txt", mes+"\n");
            lDSource.open();
            LocationUnit l = lDSource.findFirstRow();
            lDSource.close();
            if (l != null) {
                writeToFile("query.txt" , "lat = " + l.getLat() +"\nlon = "+ l.getLon()  +
                        " 1 , 1 , 1 \n\n");
                writeToFile("path.txt" , counter ,l.getLat() , l.getLon() , 1,1,1);

                GoogleSpectrumQuery.query(l.getLat() + LAT_SHIFT, l.getLon() + LON_SHIFT, 1, 1, 1);
            }
            firstQuery = false;
            secondQuery = true;
        }

        private void writeToFile(String fileName, String numOfLocat) {
            // Write to a file
            File f = getExternalFilesDir(null);
            File file = new File(f, fileName);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
                bos.write((numOfLocat).getBytes());
                bos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeToFile(String fileName, int counter  ,double lat, double lon, double chFLat, double chFLon, double numOfLoc) {
            // Write to a file
            File f = getExternalFilesDir(null);
            File file = new File(f, fileName);
            String str = lat + "," + lon + "\n";

                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
                    if (numOfLoc > 1) {
                        for (int i = 1; i <= numOfLoc; i++) {
                            double la = lat + DIF_LL_100_M  * i * chFLat;
                            double lo = lon + DIF_LL_100_M  * i * chFLon;
                            str += la + "," + lo + "\n";
                        }
                    }

                        bos.write((counter + " - numOfLoc = " + numOfLoc + "\n" + str).getBytes());
                        bos.close();
                } catch (FileNotFoundException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
