import sys
import colorsys
from PIL import Image

def rgb_to_hsl(rgb):
    r, g, b, a = rgb
    if a == 0:
        return 0, 0, 0

    h, l, s = colorsys.rgb_to_hls(r / 255.0, g / 255.0, b / 255.0)
    return h, s, l

def get_saturation_and_lightness(image, x, y):
    rgb = image.getpixel((x, y))
    hsl = rgb_to_hsl(rgb)
    return hsl[1], hsl[2]

def get_result_path(image_path):
    return image_path.replace(".png", "_paintable.png")

def process_image(image_path):
    # Opens the image in RGBA mode to preserve the alpha channel
    image = Image.open(image_path).convert("RGBA")

    # Gets the dimensions of the image
    width, height = image.size

    # Creates a new image to store the results in L mode
    new_image = Image.new("LA", (width, height))

    # Max saturation and lightness
    max_saturation = 0
    max_lightness = 0
    for y in range(height):
        for x in range(width):
            sat, light = get_saturation_and_lightness(image, x, y)
            max_saturation, max_lightness = (max(max_saturation, sat), max(max_lightness, light))

    # Iterates over each pixel of the image
    for y in range(height):
        for x in range(width):
            saturation_value = 0
            lightness_value = 0

            # Gets the alpha channel of the original pixel
            alpha = image.getpixel((x, y))[3]

            if alpha != 0:
                sat, light = get_saturation_and_lightness(image, x, y)
                saturation_value = 1 - (max_saturation - sat)
                lightness_value = 1 - (max_lightness - light)

            # Calculates the new color based on saturation and lightness
            r, g, b = colorsys.hls_to_rgb(0, lightness_value, saturation_value)

            # Sets the new color in the new image with the same alpha channel
            grayscale_color = round(g * 255)
            new_image.putpixel((x, y), (grayscale_color, alpha))

    # Saves the new image
    new_image.save(get_result_path(image_path))

if __name__ == "__main__":
    if len(sys.argv) == 1 or sys.argv[1] == "--help":
        print("Generates a paintable image for the head generator, as it remains more faithful to the original color.\n\nUsage: python generate_paintable_image.py <image_path>")
        sys.exit(1)

    if len(sys.argv) != 2:
        print("Usage: python generate_paintable_image.py <image_path>")
        sys.exit(1)

    image_path = sys.argv[1]
    process_image(image_path)
    result_path = get_result_path(image_path)
    print("Processed image saved in {}".format(result_path))
