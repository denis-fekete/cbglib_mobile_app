package cv.cbglib.detection

/**
 * Data class containing info about letterboxing that was done in ImageAnalyzer.
 *
 * In short, images must for ONNX model
 * must be scaled to 640x640 pixels. In order to not lose data a letterboxing method is applied. Meaning that image
 * from camera is scaled to match model expected image size. This bigger dimension of from camera (width or height) is
 * used for scaling, meaning the lesser dimension wil have blank spots on its side (if width is less than height, width
 * will have blank spots on left and right). These blank spots are filled with default value. In order to restore this
 * image for drawing detection and detecting clicks or (on touch events) the data must be stored
 */
data class ImageDetails(
    // used for scaling camera image into a model image size, used for reverse scaling to properly display detections
    val scale: Float,
    // padding applied to in X axis, meaning camera image width<height and X axis was filled with default value
    val padX: Int,
    // padding applied to in Y axis, meaning camera image height<camera and Y axis was filled with default value
    val padY: Int
)
