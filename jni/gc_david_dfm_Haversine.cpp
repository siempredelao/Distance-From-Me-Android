#include <jni.h>
#include <math.h>

namespace gc_david_dfm_Haversine {
    #define RADIUS 6371000

    static jdouble toRadians(jdouble angdeg){
        return angdeg / 180 * M_PI;
    }

    static jdouble JNICALL getDistanceJNI(JNIEnv *env, jclass clazz, jdouble lat_a, jdouble lon_a, jdouble lat_b, jdouble lon_b){
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

    static JNINativeMethod method_table [] = {
        {"getDistanceJNI", "(DDDD)D", (void *) getDistanceJNI}
    };
}

using namespace gc_david_dfm_Haversine;

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    } else {
        // Get jclass with env->FindClass.
        jclass clazz = env->FindClass("gc/david/dfm/Haversine");
        if (clazz){
            // Register methods with env->RegisterNatives.
            env->RegisterNatives(clazz, method_table, sizeof(method_table) / sizeof(method_table[0]));
            env->DeleteLocalRef(clazz);
            return JNI_VERSION_1_6;
        } else {
            return -1;
        }
    }
}
