from os.path import join
from pathlib import Path

import cv2
import numpy as np

def nparray_to_str(x):
    return '\n'.join([','.join(str(x[i])[1:-1].split()) for i in range(len(x))])

def generate_data_centroids(src_img, dst_folder, k):
    clusters_path = join(dst_folder, 'ClusterValues.txt')

    Path(dst_folder).mkdir(parents=True, exist_ok=True)

    img = cv2.imread(src_img).reshape((-1, 3)).astype(np.float32)
    with open(points_path, 'w') as f:
        f.write(nparray_to_str(img))

    np.random.seed(42)
    centroids = [img[0]]
    dimension = 3
    centers = k

    for _ in range(1, centers):
        dist_sq = np.array([min([np.inner(c - x, c - x) for c in centroids]) for x in img])
        probs = dist_sq / dist_sq.sum()
        cumulative_probs = probs.cumsum()
        r = np.random.rand()

        for j, p in enumerate(cumulative_probs):
            if r < p:
                i = j
                break
        centroids.append(img[i])

    with open("ClusterValues.txt", "w") as file:
        for centroid in centroids:
            for value in range(dimension):
                if value == (dimension - 1):
                    file.write(str(round(centroid[value], 4)))
                else:
                    file.write(str(round(centroid[value], 4)) + ",")
            file.write("\n")

# this function converts an image into a dataset with 3 columns(RGB) of pixel values.
# and further choose initial centroids using kmeans++ from scikit-learn documentation.
# input: file path of source image, path of folder where the dataset and initial centroids needs to be created, k.
generate_data_centroids(FILEPATHS.SOURCE_IMG, FILEPATHS.OUTPUT_FOLDER, 3)
