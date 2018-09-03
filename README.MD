 In a variety of applications, ranging from astronomical imaging to biological microscopy, the predominant source of noise follows a Poisson distribution due to the quantum nature of the photon-counting process at the detectors. The observation model for a linear degradation caused by blurring and Poisson noise (and possibly additive Gaussian noise) is given by

$\my = \alpha\ \mathcal{P}\left(\frac{\bH\mx}{\alpha}\right) + \mathcal{N}(\mathbf{0},\sigma^2\bOne)$

where {y} denotes the distorted observation of the unknown true image {x}. {H} implements a convolution of the point spread function. {P} represents the effect of Poisson noise.

Our approach is to approximate the deconvolution process as a linear combination of elementary processings of the image, termed "linear expansion of thresholds (LET)" whose coefficients are obtained by minimizing a quadratic criterion (the Poisson unbiased risk estimate - PURE). Each elementary processing consists of Wiener filtering followed by adaptive thresholding in wavelet domain. The principle is shown as follows: 