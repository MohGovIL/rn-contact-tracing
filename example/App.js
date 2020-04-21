import * as React from 'react';
import {createStackNavigator} from '@react-navigation/stack';
import {NavigationContainer} from '@react-navigation/native';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import HomeScreen from './src/Home'
import ResultsScreen from './src/Results'
import ScansScreen from './src/Scans'


const ResultsStack = createStackNavigator();

function ResultsStackScreen() {
    return (
        <ResultsStack.Navigator>
            <ResultsStack.Screen name="Scanned Contacts" component={ResultsScreen}/>
            <ResultsStack.Screen name="Scans" component={ScansScreen} options={({ route })=>({
                title: route.params.pubKey+'\'s Scans' })}/>
        </ResultsStack.Navigator>
    );
}

function HomeStackScreen() {
    return (
        <ResultsStack.Navigator>
            <ResultsStack.Screen
                name="Control Panel"
                component={HomeScreen}
            />
        </ResultsStack.Navigator>
    );
}


const Tab = createBottomTabNavigator();

function MyTabs() {
    return (
        <Tab.Navigator
            tabBarOptions={{
                activeTintColor: 'blue',
                labelPosition: 'beside-icon'
            }}>
            <Tab.Screen name="Home" component={HomeStackScreen}/>
            <Tab.Screen name="Results" component={ResultsStackScreen}/>
        </Tab.Navigator>
    );
}

export default function App() {
    return (
        <NavigationContainer>
            <MyTabs/>
        </NavigationContainer>
    );
}
