import {PermissionsAndroid} from "react-native";


// request/check location permission (only for Android)
export async function checktLocationPermission() {
    try {
        return PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION)
    } catch (err) {
        console.warn(err);
    }
}

export async function requestLocationPermission() {
    try {
        let isGranted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION);
        return (isGranted==='granted')
    } catch (err) {
        console.warn(err);
    }
}





