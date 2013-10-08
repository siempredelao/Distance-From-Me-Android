#include "gc_david_dfm_Haversine.h"
#include <math.h>

#define RADIUS 6371000

static double toRadians(jdouble angdeg){
    return angdeg / 180 * M_PI;
}

JNIEXPORT jdouble JNICALL Java_gc_david_dfm_Haversine_getDistanceJNI (JNIEnv *env, jclass clazz, jdouble lat_a, jdouble lon_a, jdouble lat_b, jdouble lon_b){
    jdouble lat1 = toRadians(lat_a);
    jdouble lat2 = toRadians(lat_b);
    jdouble lon1 = toRadians(lon_a);
    jdouble lon2 = toRadians(lon_b);

    jdouble dLat = lat2 - lat1;
    jdouble dLon = lon2 - lon1;

    jdouble sinlat = sin(dLat / 2);
    jdouble sinlon = sin(dLon / 2);

    jdouble a = (sinlat * sinlat) + cos(lat1)*cos(lat2)*(sinlon*sinlon);
    jdouble aux;
    if (sqrt(a) < 1.0) aux = sqrt(a);
    else aux = 1.0;
    jdouble c = 2 * asin(aux);

    return RADIUS * c;
}

