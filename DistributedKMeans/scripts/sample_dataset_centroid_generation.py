from sklearn.datasets import make_blobs
import pandas as pd
import numpy as np
from pandas.plotting._matplotlib import scatter_matrix
from matplotlib import pyplot
from pandas import DataFrame

dimension = 3
samples = 1000
centers = 4

points, y = make_blobs(n_samples=samples, centers=centers, n_features=dimension)

with open("dataset.txt", "w") as file:
    for centroid in points:
        for value in range(dimension):
            if value == (dimension - 1):
                file.write(str(round(centroid[value], 4)))
            else:
                file.write(str(round(centroid[value], 4)) + ",")
        file.write("\n")

data = np.array(points)

# plot
df = pd.DataFrame(data, columns=['x1', 'x2', 'x3'])
scatter_matrix(df, alpha=0.2, figsize=(10, 10))

df = DataFrame(dict(x=points[:, 0], y=points[:, 1], label=y))
colors = {0: 'red', 1: 'blue', 2: 'green', 3: 'black', 4: 'purple', 5: 'pink', 6: 'orange'}
fig, ax = pyplot.subplots()
grouped = df.groupby('label')
for key, group in grouped:
    group.plot(ax=ax, kind='scatter', x='x', y='y', label=key, color=colors[key])
pyplot.show()


# generate cluster centroids
np.random.seed(42)
centroids = [data[0]]

for _ in range(1, centers):
    dist_sq = np.array([min([np.inner(c - x, c - x) for c in centroids]) for x in data])
    probs = dist_sq / dist_sq.sum()
    cumulative_probs = probs.cumsum()
    r = np.random.rand()

    for j, p in enumerate(cumulative_probs):
        if r < p:
            i = j
            break
    centroids.append(data[i])

with open("ClusterValues.txt", "w") as file:
    for centroid in centroids:
        for value in range(dimension):
            if value == (dimension - 1):
                file.write(str(round(centroid[value], 4)))
            else:
                file.write(str(round(centroid[value], 4)) + ",")
        file.write("\n")