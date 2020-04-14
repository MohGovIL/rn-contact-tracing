import {check, request, PERMISSIONS, RESULTS} from 'react-native-permissions';

export function requestLocationPermssion() {
    request(PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION)
        .then((result) => {
            switch (result) {
                case RESULTS.UNAVAILABLE:
                    alert('UNAVAILABLE')
                    break;
                case RESULTS.DENIED:
                    console.log(
                        alert('DENIED')
                    );
                    break;
                case RESULTS.GRANTED:
                    alert('GRANTED')
                    break;
                case RESULTS.BLOCKED:
                    alert('BLOCKED')
                    break;
            }
        })
        .catch((error) => {
            // …
        });
}
