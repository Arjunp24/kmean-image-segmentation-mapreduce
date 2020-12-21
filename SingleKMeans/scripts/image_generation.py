import FILEPATHS
import cv2
import numpy as np

def get_cluster_array(path):
  arr = []
  with open(path) as my_file:
      for line in my_file:
          temp = []
          for i in line[:-2].split(','):
            temp.append(float(i))
          arr.append(temp)
  return np.array(arr)

def generate_new_image(cluster_path, dest_img_path, src_img_path):

    clusters = get_cluster_array(cluster_path)
    img = cv2.imread(src_img_path)
    shape = img.shape

    img = img.reshape((-1, 3))
    new_image = np.zeros_like(img)
    for i in range(img.shape[0]):
        ind = np.linalg.norm(clusters - img[i], axis=-1).argmin()
        new_image[i] = clusters[ind].astype(np.uint8)

    cv2.imwrite(dest_img_path, new_image.reshape(shape))

# this function generates the new image given the source image and new clusters.
# input: filepath of cluster centers, filepath of destination image, filepath of source image
generate_new_image(FILEPATHS.FINAL_CLUSTERS, FILEPATHS.DESTINATION_IMAGE, FILEPATHS.SOURCE_IMAGE)
