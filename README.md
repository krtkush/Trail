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
