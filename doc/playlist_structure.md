# Playlist 

A Playlist shall contain a list of songs, and the assigned meta-data for them. 
The playlist language shall be JSON. 
The playlist shall be encrypted.
The Extension of the playlist shall be *.sap(Salsa Assistant Playlist).

## Structure
The Playlist file shall contain the following labels: 
 - the name of the playlist is decided based on the filename without the extension
 - `netLocation`: The location of a preferred Neural Net, optional
 - `lastSVar`: Last Selected Variant
 
Any other label shall represent a variant, which shall be a JSON object.
The Variant JSON object shall contain 
 - A unique Identifier
 - The songs as a JsonObject, described under song_data.md
 - `lastSInd`: Last selected song index inside the variant