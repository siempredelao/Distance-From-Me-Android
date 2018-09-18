#
# Copyright (c) 2018 David Aguiar Gonzalez
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Support Design
#-dontwarn android.support.design.**
#-keep class android.support.design.NavigationView { *; }
#-keep class android.support.design.Snackbar { *; }
#-keep interface android.support.design.** { *; }
#-keep public class android.support.design.R$* { *; }


# Support
#-keep class android.support.v7.app.ActionBar { *; }
#-keep class android.support.v7.app.ActionBarDrawerToggle { *; }
#-keep class android.support.v7.app.AppCompatActivity { *; }
#-keep class android.support.v7.widget.DefaultItemAnimator { *; }
#-keep class android.support.v7.widget.LinearLayoutManager { *; }
#-keep class android.support.v7.widget.RecyclerView { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class android.support.v7.widget.ShareActionProvider { *; }
#-keep class android.support.v7.widget.Toolbar { *; }
#-keep class android.support.v7.preference.Preference { *; }
#-keep class android.support.v7.preference.PreferenceFragmentCompat { *; }