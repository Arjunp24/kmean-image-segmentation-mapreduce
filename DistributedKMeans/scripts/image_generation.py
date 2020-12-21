import FILEPATHS
import cv2
import numpy as np


def get_cluster_array(path):
    arr = {}
    ctr = -1
    with open(path) as my_file:
        for line in my_file:
            temp = []
            if (line.startswith("C")):
                ctr += 1
                arr[ctr] = [];
            elif len(line.strip())>10:
                for i in line[:-2].split(','):
                    temp.append(float(i))
                arr[ctr].append(temp);


    return arr


def generate_new_image(cluster_path, src_img_path, dest_img_path):
    clusters = get_cluster_array(cluster_path)
    x = 4;
    for cluster in clusters.values():
        cluster = np.array(cluster)
        print("Clusters"+str(cluster))
        img = cv2.imread(src_img_path)
        shape = img.shape

        img = img.reshape((-1, 3))
        new_image = np.zeros_like(img)
        for i in range(img.shape[0]):
            ind = np.linalg.norm(cluster - img[i], axis=-1).argmin()
            new_image[i] = cluster[ind].astype(np.uint8)

        cv2.imwrite(dest_img_path + str(x)+".jpg", new_image.reshape(shape))
        x += 1;


generate_new_image(FILEPATHS.FINAL_CLUSTERS, FILEPATHS.SOURCE_IMAGE, FILEPATHS.DESTINATION_IMAGE)
