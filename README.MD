 In a variety of applications, ranging from astronomical imaging to biological microscopy, the predominant source of noise follows a Poisson distribution due to the quantum nature of the photon-counting process at the detectors. The observation model for a linear degradation caused by blurring and Poisson noise (and possibly additive Gaussian noise) is given by

<p align="center">
<img src="Resources/model.png" alt="{\my = \alpha\ \mathcal{P}\left(\frac{\bH\mx}{\alpha}\right) + \mathcal{N}(\mathbf{0},\sigma^2\bOne)}" width='200px'/>
 </p>

<p> where <img src="Resources/y.png" alt="{y}" margin-top="-4px;" width="60px"/> denotes the distorted observation of the unknown true image <img src="images/x.png" alt="{x}" style="vertical-align: -3px; width:60px"/>. <img src="images/H.png" alt="{H}" style="vertical-align: -2px; width:70px"/> implements a convolution of the point spread function. <img src="images/P.png" alt="{P}" style="vertical-align: -4px; width:30px"/> represents the effect of Poisson noise. </p>

<p> Our approach is to approximate the deconvolution process as a linear combination of elementary processings of the image, termed "linear expansion of thresholds (LET)" whose coefficients are obtained by minimizing a quadratic criterion (the Poisson unbiased risk estimate - PURE). Each elementary processing consists of Wiener filtering followed by adaptive thresholding in wavelet domain. The principle is shown as follows:

<p align="center">
<img class="centered" src="Resources/principle.png" alt="{Principle}" width="550px"/>
</p>
