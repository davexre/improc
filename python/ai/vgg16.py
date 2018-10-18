import keras as K
import numpy as np
import os
import h5py
import json

# K.applications.vgg16.VGG16(include_top=True, weights='imagenet', input_tensor=None, input_shape=None, pooling=None, classes=1000)

def ensure_dir(file_path):
    directory = os.path.dirname(file_path)
    if not os.path.exists(directory):
        os.makedirs(directory)

outdir = '../target/vgg16/'
ensure_dir(outdir + 'a')
f = h5py.File(os.path.expanduser('~/.keras/models/vgg16_weights_tf_dim_ordering_tf_kernels.h5'), mode='r')
layer_names = f.attrs['layer_names']
lnames = list()
for layer_name in layer_names:
    g = f[layer_name]
    weight_names = g.attrs['weight_names']
    lweights = list()
    for weight_name in weight_names:
        lweights.append(weight_name[:-4])

        #weights = np.asarray(g[weight_name]).tolist()
        #fou_name = outdir + weight_name[:-4] + '.json'
        #print(fou_name)
        #fou = open(fou_name,"w+")
        #obj = {}
        #obj[weight_name] = weights
        #fou.writelines(json.dumps(obj))
        #fou.close()

    lnames.append([layer_name, lweights])
fou = open(outdir + 'alllayers.json',"w+")
fou.writelines(json.dumps(lnames, indent=4))
fou.close()

