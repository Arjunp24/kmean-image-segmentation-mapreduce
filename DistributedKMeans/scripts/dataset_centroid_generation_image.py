from os.path import join
from pathlib import Path

import cv2
import numpy as np

import FILEPATHS


def nparray_to_str(x):
    return '\n'.join([','.join(str(x[i])[1:-1].split()) for i in range(len(x))])

def generate_data_centroids(src_img, dst_folder, k):
    points_path = join(dst_folder, 'dataset.txt')
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

    with open(clusters_path, "w") as file:
        for centroid in centroids:
            for value in range(dimension):
                if value == (dimension - 1):
                    file.write(str(round(centroid[value], 4)))
                else:
                    file.write(str(round(centroid[value], 4)) + ",")
            file.write("\n")

generate_data_centroids(FILEPATHS.SOURCE_IMAGE, FILEPATHS.OUTPUT_FOLDER, 30)
