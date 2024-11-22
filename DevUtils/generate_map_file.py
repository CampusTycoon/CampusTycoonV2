import argparse

import ujson
from PIL import Image

TILE_COLOUR_MAPPINGS = {(0, 255, 0): "GRASS", (0, 0, 255): "WATER"}
BUILDING_COLOUR_MAPPINGS = {(0, 0, 0): "ROAD"}

# Example Usage:
# > python generate_map_file.py map64x32.png -b map64x32.png


def create_map(map_in_img, out_json, buildings_in_img=None):
    # Open the image file
    image = Image.open(map_in_img)
    image = image.convert("RGB")

    width, height = image.size

    extracted_map = []
    buildings = []

    # Iterate through each pixel
    for y in range(height):
        extracted_map.append([])

        for x in range(width):
            # Add the tile with specified mapping to the array
            col = image.getpixel((x, y))
            if col not in TILE_COLOUR_MAPPINGS:
                raise Exception(f"Colour {col} not listed in TILE_COLOUR_MAPPINGS")

            extracted_map[y].append(TILE_COLOUR_MAPPINGS[col])

    # If a building image is supplied also add this
    if buildings_in_img:
        # Open the image file
        image = Image.open(buildings_in_img)
        image = image.convert("RGB")

        building_width, building_height = image.size
        if building_width != width or building_height != height:
            raise Exception(f"Building image is not same dimensions as map image.")

        # Iterate through each pixel
        for y in range(height):
            for x in range(width):
                # Add the building with specified mapping to the array
                col = image.getpixel((x, y))
                if col in BUILDING_COLOUR_MAPPINGS:
                    buildings.append({"name": BUILDING_COLOUR_MAPPINGS[col], "x": x, "y": y})

    # Write the map into the json file
    map_contents = {"map": extracted_map, "buildings": buildings}
    with open(out_json, "w") as f:
        ujson.dump(map_contents, f)


# Parse arguments to get the input image file and output json file names
parser = argparse.ArgumentParser()
parser.add_argument("map_path", type=str, help="Input Map Image Path")
parser.add_argument("-b", "--buildings_path", type=str, help="Input Buildings Image Path")
parser.add_argument("-o", "--out", type=str, help="Output JSON Path", default="map.json")
args = parser.parse_args()


create_map(args.map_path, args.out, args.buildings_path)
