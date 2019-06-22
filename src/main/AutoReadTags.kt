package main

import kotlin.jvm.internal.Intrinsics

class AutoReadTags {
    var fileName = "FILESTOREFILENAME1="
    var fileName2 = "FILESTOREFILENAME2="
    var x = "X(Width)="
    var y = "Z(Depth)="
    var bytesPerPixel = "VoxelBits="
    var patient = AutoReadTags.Patient()

    class Patient {
        var id = "Patient ID="
        var surName = "First name="
        var lastName = "Family Name="
        var birthday = "Date of Birth="
        var eye = "Eye="
        var commentary = "Description="
    }
}
