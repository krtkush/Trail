# Trail

Currently in beta, Trail aims to be a simple and highly customizable user behaviour tracking library for Android. 

Unlike other, industry standard tools, Trail only provides raw data within the app with no web interface. It'll be upto the developer's own discretion as to how to use the data collected.## Setup

## Setup

[![Release](https://jitpack.io/v/krtkush/Trail.svg)](https://jitpack.io/#krtkush/Trail)

Setup is pretty straight forward. 
In your project's `build.gradle` add the following - 

    allprojects {
      repositories {
          jcenter()
          maven { url "https://jitpack.io" }
      }
    }
    
And, in your app's `build.gradle` add this under `dependencies` block -

    compile 'com.github.krtkush:Trail:<version_available_on_jitpack>'
    
example - `compile 'com.github.krtkush:Trail:v1.0.0_beta_1'`

## Usage

At the moment, Trail only enables you to track time a user spends looking at the items present in a RecyclerView.

Initializing Trail - 

    Trail trail = new Trail.Builder()
                .setRecyclerView(recyclerView)      // Pass the instance of the RecyclerView
                .setMinimumViewingTimeThreshold(2000)   // Minimum time the view is on the screen for being tracked. 
                .setMinimumVisibleHeightThreshold(60)   // Minimum height visible on the screen (in term sof percentage).
                .setTrailTrackingListener(this)     // Listener for callbacks related to Trail.
                .setDataDumpInterval(1000)      // Time after which data will be handed over to the user automatically. 
                .dumpDataAfterInterval(true)    // Enable handing data over to user after the time period defined above.
                .build();
                
Available method - 

1. `startTracking()` - Start tracking.
2. `pauseTracking()` - Pause tracking.
3. `resumeTracking()` - Resume tracking.
4. `trackingStatus()` - Get the current status of Trail (paused or activte).
5. `getTrackingData(boolean stopTracking)` - Get the tracking data collected till this moment. `stopTracking` To stop the tracking or not. 
6. `clearAllTrackingData()` - Clea all the tracking data collected.
                
            
