package com.chapresearch.nasaconnect;

//
// NASA_BLE_Interface - the following interface is used to interact with the NASA
//                      BLE system. In particular, it is meant to be used by the
//                      controller as communication comes from/to the contributors.
//
//                      The NASA machinery keeps track of the slot usage so that
//                      the UI doesn't have to worry about it. This means:
//
//			- Slots are either IN USE or NOT IN USE
//			- If a slot isn't in use, a contributor can "claim" it by sending
//			  sending an "i'm alive" to the slot - the contributor will get a
//                        succeed/fail based upon whether the slot was claimed.
//			- When a slot is in use, a different contributor can't grab it
//			  because the "i'm alive" will fail.
//			- A slot can "time-out" - that is, if it hasn't heard from the
//			  contributor who "has" it for a defined length of time, the the
//			  slot is considered not in use
//			- The UI gets a callback when a slot is claimed, or when a slot
//			  is abandoned. There is no useful data in the callback, that comes
//			  from the other callbacks.
//			- The UI can simply trust the other callbacks, displaying the data
//			  that comes in for the given slot. It doesn't have to worry about
//			  the owner of the slot.
//			

public interface NASA_BLE_Interface {

    //
    // NASA_controllerName() - called when the contributor wants to get the name of the controller.
    //                         The controller code should return a String with the name in it.
    //                         The name returned must be 10 characters or less.
    //
    String NASA_controllerName();

    //
    // NASA_passwd() - called by the contributor to ensure that we have the "secret" to connect
    //                 to the controller. NOTE that this isn't intended to be secure. It's just
    //                 meant to keep the riff-raff out. :-)
    //                 The password returned must be 10 characters or less.
    //
    String NASA_password();

    //
    // NASA_match() - called by the controller to get the current match number. This number is
    //                read by Contributors, plus the characteristic is the one that Contributors
    //                listen for notifications. When they get a notification on the match, they
    //                reset and prepare for the next match.
    //
    String NASA_match();

    //
    // NASA_slotChange() - called when there is a change to one of the slots. If "claimed" is TRUE
    //			   then the slot has just been claimed. If FALSE, the slot has just been
    //			   abandoned. Note that the UI will get a FALSE call followed by a TRUE call
    //			   when a claimed slot changes hands.
    //
    void NASA_slotChange(int slot, boolean claimed);

    //
    // NASA_teamColor() - called when the contributor reports the color of the given team.
    //                    0x01 = BLUE, 0x02 = RED, anything else means no-color-chosen.
    //
    void NASA_teamColor(int slot, int color);

    //
    // NASA_teamNumber() - called when the contributor reports the name of the team.
    //
    void NASA_teamNumber(int slot, String number);

    //
    // NASA_competition() - called before uploading data to get the name of the competition
    //                      to attach to the data.
    //
    String NASA_competition();

    //
    // NASA_year() - called before uploading data to get the year of the competition to
    //               attach to the data.
    //
    String NASA_year();
    
    //
    // NASA_contributorName() - called when the contributor reports the name of the contributor.
    //
    void NASA_contributorName(int slot, String name);

    //
    // NASA_dataTransmission() - called after the contributor sends data. The data is in JSON format
    //                           so it is just a string - though it can be a bit long.  Note that
    //                           the data is automatically stored, so this call is just for info.
    //                           The UI isn't expected to do anything with the data.  The "finalChunk"
    //                           indicates if this was the final transmission.  Note that the UI should
    //                           clear any indicators of data if "finalChunk" is false. This is used
    //                           by the interface to let the UI know that it should clear the UI
    //                           element (done when resetting).
    //
    void NASA_dataTransmission(int slot, boolean finalChunk, String jsonData);

    //
    // NASA_dataUploadStatus() - called when return status from data transmission to the DB is
    //                           received. The UI can indicate success or failure if desired.
    //                           Status is simple, true = success.
    //
    void NASA_dataUploadStatus(int slot, boolean success);
    
}
