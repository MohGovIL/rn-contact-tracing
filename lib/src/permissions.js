import {PermissionsAndroid} from "react-native";


// request/check location permission (only for Android)
export async function checktLocationPermission() {
    try {
        return PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION) && PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION)
    } catch (err) {
        console.warn(err);
    }
}

export async function requestLocationPermission() {
    try {
        let isGranted = await PermissionsAndroid.requestMultiple([PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION, PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION]);
        return (isGranted==='granted')
    } catch (err) {
        console.warn(err);
    }
}





