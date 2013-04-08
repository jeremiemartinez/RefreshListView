#RefreshListView 

Simple Android ListView that enables pull to refresh as Twitter or Facebook apps ListView. Compatible with API 7 (Android 2.1).

[![RefreshListView Presentation](../../raw/master/images/presentation_listview_small.png)](../../raw/master/images/presentation_listview.png)

## Usage

Please check out the [example project](https://github.com/jeremiemartinez/RefreshListView/tree/master/refreshlistview_examples).

If you need more technical information, you can refer to the javadoc I wrote in the [RefreshListView class](https://github.com/jeremiemartinez/RefreshListView/tree/master/refreshlistview_library/src/com/github/jeremiemartinez/refreshlistview/RefreshListView.java).

### How to integrate it ?

#### Eclipse
Since RefreshListView is an apklib, you have to [download it](https://github.com/jeremiemartinez/RefreshListView/archive/master.zip) and import the **refreshlistview_library** folder in your Eclipse.

Then you need to go to Properties > Android in your project and add RefreshListView as a Library.

#### Maven

``` xml
<dependency>
   <groupId>com.github.jeremiemartinez</groupId>
   <artifactId>refreshlistview</artifactId>
   <type>apklib</type>
   <version>1.4</version>
</dependency>
```

### How to use it ?

#### Layout:

``` xml
  <com.github.jeremiemartinez.refreshlistview.RefreshListView
  android:id="@+id/list"
  android:layout_height="match_parent"
  android:layout_width="match_parent" />
```

#### Activity:

``` java

  // onCreate method
  @Override
  public void onCreate(Bundle savedInstanceState) {
    [...]
    // Add callback to RefreshListView
    list.setRefreshListener(new OnRefreshListener() {

      @Override
      public void onRefresh(RefreshListView listView) {
        // Task to do while refreshing
        new BackgroundTask().execute();

      }
    });
    [...]
  }

  [...]
  
  // Background AsyncTask
  private class BackgroundTask extends AsyncTask<Void, Void, String> {
    
    @Override
    protected String doInBackground(Void... params) {
      [...] // Do background task
    
      return string;
    }
    
    @Override
    protected void onPostExecute(String newApi) {
      [...] // Update adapter
    
      // call on RefreshListView to hide header and notify the listview, refreshing is done
      list.finishRefreshing();
    }
  }
```
    
#### Enable and use Update date/time:
    
Another feature implemented in this project, is to allow user to visualize the last update date and time. To do so, just use the following method:
``` java
  list.setEnabledDate(true);
```
This method is overloaded and you can add a first date to start your list at loading as follow:
``` java
  list.setEnabledDate(true, new Date());
```
Finally, you can also get the last update date thanks to the getter:
``` java
  Date lastUpdate = list.getLastUpdateDate();
```
    
#### String resources and Localization:
    
If you want to change the default strings in the header, just add the following resources into your strings.xml file and change the values:
``` xml
  <?xml version="1.0" encoding="utf-8"?>
  <resources>
        
    <string name="refreshlistview_updating">Updating…</string>
    <string name="refreshlistview_release">Release to refresh…</string>
    <string name="refreshlistview_pulldown">Pull down to refresh…</string>
        
  </resources>
```
    
Since, you can use the strings.xml file, you can also integrate localization. For instance, create a new folder according to
[Android documentation](http://developer.android.com/guide/topics/resources/localization.html#using-framework): in my case values-fr.
Finally add the strings as before:
``` xml
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        
      <string name="refreshlistview_updating">Mise à jour…</string>
      <string name="refreshlistview_release">Relâcher pour actualiser…</string>
      <string name="refreshlistview_pulldown">Tirer pour actualiser…</string>
        
    </resources>
```

#### Customizing styles

If you want to customize the style of the header, you can overload the following styles in your styles.xml, so your styles will be used instead of ours:
``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="refreshlistview_container">
        [...]
    </style>

    <style name="refreshlistview_arrow">
        [...]
    </style>
    
    <style name="refreshlistview_progress">
        [...]
    </style>

    <style name="refreshlistview_comment">
        [...]
    </style>

    <style name="refreshlistview_date">
        [...]
    </style>

    <style name="refreshlistview_header">
        [...]
    </style>

</resources>

```
    
#### Simple error handling:
    
To handle error in task, this project provides a simple method that will display a toast in case something went wrong in refresh task:
``` java
  list.errorInRefresh("Something went wrong.");
```
    
## Developed by
  * Jeremie Martinez - [jeremiemartinez@gmail.com](mailto:jeremiemartinez@gmail.com)
    
    
## Licence
    
Copyright 2012-2013 - Jeremie Martinez ([jeremiemartinez@gmail.com](mailto:jeremiemartinez@gmail.com))
    
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
